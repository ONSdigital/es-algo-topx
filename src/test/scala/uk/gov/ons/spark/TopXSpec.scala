package uk.gov.ons.spark

import com.github.mrpowers.spark.fast.tests.DataFrameComparer
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.sql.types._
import org.scalatest.FunSpec

trait SparkSessionTestWrapper {

  lazy implicit val spark: SparkSession = {
    SparkSession
      .builder()
      .master("local[*]")
      .appName("TopX")
      .getOrCreate()
  }

  spark.sparkContext.setLogLevel("WARN")

}

class TopXSpec
  extends FunSpec
    with DataFrameComparer
    with SparkSessionTestWrapper {

  import spark.implicits._

  private val sourceDF = Seq(
    ("Blocks R us", "NE", 11),
    ("Blocks R us", "NE", 35),
    ("Blocks R us", "SW", 5),
    ("Blocks R us", "SW", 8),
    ("Big Bad Block Company", "SW", 27),
    ("Big Bad Block Company", "NE", 23),
    ("Big Bad Block Company", "NW", 56)
  ).toDF("warehouse", "region", "stock")


  describe("Calculate Top X Function") {

    it("calculates top total values grouped by column") {

      val actualDF = TopX.apply().calculateTopX(sourceDF, "region", "stock").right.get

      actualDF.show()

      val expectedData = Seq(
        Row("SW", 27.0),
        Row("NE", 35.0),
        Row("NW", 56.0)
      )

      val expectedSchema = List(
        StructField("region", StringType, true),
        StructField("1", DoubleType, false)
      )

      val expectedDF = spark.createDataFrame(
        spark.sparkContext.parallelize(expectedData),
        StructType(expectedSchema)
      )

      assertSmallDataFrameEquality(actualDF.orderBy("region"), expectedDF.orderBy("region"))

    }

    it("calculates top 2 total values grouped by column filling in blanks with 0.0") {

      val actualDF = TopX.apply().calculateTopX(sourceDF, "region", "stock", 2).right.get

      actualDF.show()

      val expectedData = Seq(
        Row("SW", 27.0, 8.0),
        Row("NE", 35.0, 23.0),
        Row("NW", 56.0, 0.0)
      )

      val expectedSchema = List(
        StructField("region", StringType, true),
        StructField("1", DoubleType, false),
        StructField("2", DoubleType, false)
      )

      val expectedDF = spark.createDataFrame(
        spark.sparkContext.parallelize(expectedData),
        StructType(expectedSchema)
      )

      assertSmallDataFrameEquality(actualDF.orderBy("region"), expectedDF.orderBy("region"))

    }

    it("returns single exception if groupBy column does not exist in inputDF") {

      val exceptionMsg = "'area' column does not exist in input dataframe"

      val errors = TopX.apply().calculateTopX(sourceDF, "area", "stock").left.get

      assert(errors.size == 1)
      assert(errors.head.getMessage === exceptionMsg)

    }

    it("returns two exceptions if both groupBy and orderBy columns do not exist in inputDF") {

      val exceptionMsg1 = "'area' column does not exist in input dataframe"
      val exceptionMsg2 = "'total' column does not exist in input dataframe"

      val errors = TopX.apply().calculateTopX(sourceDF, "area", "total").left.get

      assert(errors.size == 2)
      assert(errors.head.getMessage === exceptionMsg1)
      assert(errors(1).getMessage === exceptionMsg2)

    }
  }

}
