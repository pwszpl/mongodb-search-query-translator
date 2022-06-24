# MongoDB query parser for Java
This package transforms where statements written in sql-like manner to Bson objects required by MongoDB Java engine.

# Usage
Package currently allows for transformation of following statements:
* Logical statements:
  * AND / OR statements (as well as nesting of logical statements with parenthesis)
* Comparision:
  * String field comparision with EQ or NE comparators
  * Numeric field comparision with GT, LT, EQ, NE, LTE, GTE comparators

As a result of transformation you get valid Bson object that can be pushed to MongoDB as a part of query.

## Build
`maven clean package`

## Usage
To parse String please initialize Parser with stream, and run parse method wchich will return bson object.

    MongoSearchEngineParser parser = new MongoSearchEngineParser(new StringReader(s));
    Bson filter = parser.parse();

Passing string `x.z='z'` will return equivalent Bson object `Filters.eq("x.z","z")`.

Passing string `x.y='z' and x.z='z'` will return equivalent Bson object `Filters.and(Filters.eq("x.y","z"),Filters.eq("x.z","z"))`

For more examples see [MongoSearchEngineParserTest.class](https://github.com/pwszpl/mongodb-search-parser-for-java/blob/main/src/test/java/mongo/parser/MongoSearchEngineParserTest.java).



# Bug tracker
Issues and pull requests are administered at [GitHub](https://github.com/pwszpl/mongodb-search-parser-for-java).
