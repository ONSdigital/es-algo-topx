package uk.gov.ons.spark

import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions.{col, row_number}
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.util.{Failure, Success, Try}

trait TopX {
  def calculateTopX(inputDF: DataFrame, groupBy: String, orderBy: String, noOfResults: Integer = 1)(implicit spark: SparkSession): Either[List[Throwable], DataFrame]
}

object TopX {

  private[spark] class API extends TopX {

    override def calculateTopX(inputDF: DataFrame, groupBy: String, orderBy: String, noOfResults: Integer)(implicit spark: SparkSession): Either[List[Throwable], DataFrame] = {

      val isGroupBy = validateColumn(inputDF, groupBy)
      val isOrderBy = validateColumn(inputDF, orderBy)
      val errors = List(isGroupBy, isOrderBy) collect { case Failure(err) => err }

      if (errors.isEmpty) {

        val window = Window.partitionBy(col(groupBy)).orderBy(col(orderBy).desc)
        val topX = inputDF.withColumn("rank", row_number().over(window))

        val resultDF = topX.filter(s"rank<=$noOfResults").groupBy(groupBy).pivot("rank").avg(orderBy).na.fill(0.0)

        Right(resultDF)

      } else Left(errors)
    }
  }


  def validateColumn(inputDF: DataFrame, column: String): Try[String] = {
    if (!inputDF.schema.fieldNames.contains(column))
      Failure(new IllegalArgumentException(s"'$column' column does not exist in input dataframe"))
    else Success(column)
  }

  def apply(): API = {
    new API()
  }
}
