# MongoDB search query translator for Java
This package transforms where statements written in sql-like manner to **Criteria/Bson** objects required by MongoDB Java engine.

# Capabilities
Package allows you to write human friendly filter queries in syntax similar to sql. As of now, package supports following statements:
* Logical statements:
  * AND / OR statements (as well as nesting of logical statements with parenthesis)
* Comparision:
  * String field comparision with EQ or NE comparators
  * Numeric field comparision with GT, LT, EQ, NE, LTE, GTE comparators

As a result of transformation you get valid Java object that can be pushed to MongoDB as a part of query.

## Build
`maven clean package`

## Usage
To parse String please initialize Parser with stream, and run parse method wchich will return bson object.

    String s = "`x.z='z'";
    MongoSearchEngineParser parser = new MongoSearchEngineParser(new StringReader(s));
    Bson filter = (Bson)parser.parse();

You can switch between returning Bson/Criteria object by using following methods:
    
    parser.setCriteraMode() // parse method returns Criteria object
    parser.setBSONMode() // parse method returns Bson object

If none of the above method is used, Bson object is returned by default.

### Integrating with SpringBoot
To pass resulting object to DB you can use MongoTemplate interface:

    Criteria criteria = (Criteria) parser.parse();
    parser.setCriteraMode();
    Query query = new Query(criteria);
    return mongoTemplate.find(query,Collection.class);

### Integrating with MongoDB driver
To pass resulting object to DB with mongo driver for Java, you can use Collection interface:

    Bson bson = (Bson) parser.parse();
    return collection.find(bson);

### Syntax
Parser currently supports following types of symbol:

| Symbols                     | Symbol Type                         | Type          |
|-----------------------------|-------------------------------------|---------------|
| `OR`, `or`, `\|\|`  | **Or** operator                     | Logical       |
| `AND`, `and`, `&&`          | **And** operator                    | Logical       |
| `=`                         | **Equals** operator                 | Math          |
| `<>`, `!=`, `^=`            | **Not Equals** operator             | Math          | 
| `>`                         | **Greater than** operator           | Math          | 
| `<`                         | **Lower than** operator             | Math          | 
| `<=`                        | **Lower than or equals** operator   | Math          | 
| `>=`                        | **Greater than or equals** operator | Math          |
| `'example string'`          | **String literal**                  | Field value   |
| `9` , `9.0`                 | **Positive number**                 | Field value   |
| `-9` , `-9.0`               | **Negative number**                 | Field value   |
| `field.subField`            | **Document field name**             | Field value   |

### Examples

Passing string `x.z='z'` will return:
* Bson object `Filters.eq("x.z","z")`
* Criteria object `Criteria.where("x.z").is("z")`

Passing string `x.y='z' and x.z='z'` will return:
* Bson object `Filters.and(Filters.eq("x.y","z"),Filters.eq("x.z","z"))`
* Criteria object `new Criteria().andOperator(Criteria.where("x.y").is("z"),(Criteria.where("x.z").is("z")))`

For more examples see [MongoSearchEngineParserTest.class](https://github.com/pwszpl/mongodb-search-query-translator/blob/main/src/test/java/mongo/parser/MongoSearchEngineParserTest.java).

### Important notes
1. All numbers are converted to double, no matter how they were written in original query (e.g. 10 is converted to 10.0 etc.)
2. As of now only comparision between field and value are supported (any field - field, or value-value comparision will result in parsing error)
3. As of now, there is no support for escaping `'` character in string literals. May be added in future releases.
4. **<ins>Engine does not do any optimisations of query written by user.</ins>** In case of simple queries it might not have any difference, but for very large and complex queries (many nestings of and/or operators) there might be a difference in response times. 
Please check impact on your querry resolution time, before applying to production environment.

# Bug tracker
Issues and pull requests are administered at [GitHub](https://github.com/pwszpl/mongodb-search-query-translator).
