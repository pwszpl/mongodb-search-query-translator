# MongoDB search query translator for Java
This package transforms where statements written in sql-like manner to **Criteria/Bson** objects required by MongoDB Java engine.

# Capabilities
Package allows you to write human friendly filter queries in syntax similar to sql. For a list of supported functions, read **Syntax** paragraph.

As a result of transformation you get valid Java object that can be pushed to MongoDB as a part of query.

## Build
If you use maven you can add dependency to build:

        <dependency>
            <groupId>io.github.pwszpl</groupId>
            <artifactId>mongodb-search-query-translator</artifactId>
        </dependency>

## Usage
To parse String please initialize Parser with stream, and run parse method which will return bson object.

    String s = "`x.z='z'";
    MongoSearchEngineParser parser = new MongoSearchEngineParser(new StringReader(s));
    Bson filter = (Bson)parser.parse();

You can switch between returning Bson/Criteria object by using following methods:
    
    parser.setCriteraMode() // parse method returns Criteria object
    parser.setBSONMode() // parse method returns Bson object

If none of the above method is used, Bson object is returned by default.

### Examples

Passing string `x.z='z'` will return:
* Bson object `Filters.eq("x.z","z")`
* Criteria object `Criteria.where("x.z").is("z")`

Passing string `x.y='z' and x.z='z'` will return:
* Bson object `Filters.and(Filters.eq("x.y","z"),Filters.eq("x.z","z"))`
* Criteria object `new Criteria().andOperator(Criteria.where("x.y").is("z"),(Criteria.where("x.z").is("z")))`

For more examples see [MongoSearchEngineParserTest.class](https://github.com/pwszpl/mongodb-search-query-translator/blob/main/src/test/java/mongo/parser/MongoSearchEngineParserTest.java).

For live example of usage for this plugin see [MongoDB HTML search engine](https://github.com/pwszpl/mongodb-html-search-engine).

### Integrating with Spring Boot
To pass resulting object to DB you can use MongoTemplate interface:

    parser.setCriteraMode();
    Criteria criteria = (Criteria) parser.parse();
    Query query = new Query(criteria);
    return mongoTemplate.find(query,Collection.class);

### Integrating with MongoDB driver
To pass resulting object to DB with mongo driver for Java, you can use Collection interface:

    Bson bson = (Bson) parser.parse();
    return collection.find(bson);

### Syntax
Table below shows support for [Filters](https://www.mongodb.com/docs/drivers/java/sync/v4.6/fundamentals/builders/filters/) in MongoDB driver:

#### Comparision filters
| Filter                | Supported | Syntax                                          | Example                   |
|-----------------------|-----------|-------------------------------------------------|---------------------------|
| **Comparision**       |           |                                                 |                           |
| eq                    | yes       | `=`                                             | x.y = 1                   |
| gt                    | yes       | `>`                                             | x.y > 1                   |
| gte                   | yes       | `>=`                                            | x.y >= 1                  |
| lt                    | yes       | `<`                                             | x.y < 1                   |
| lt                    | yes       | `<=`                                            | x.y <= 1                  |
| ne                    | yes       | `<>`, `!=`, `^=`                                | x.y <> 1                  |
| in                    | yes       | `<FIELD_NAME> IN ( <LITERAL> )`                 | x.y in ('x' 'y')          |
| nin                   | yes       | `<FIELD_NAME> NIN ( <LITERAL> )`                | x.y nin ('x' 'y')         |
| empty                 | no        |                                                 |                           |
| **Logical**           |           |                                                 |                           |
| and                   | yes       | `and`, `&&`                                     | x.y = 1 and x.z = 'abc'   |
| or                    | yes       | `or`, `\|\|`                                    | x.y = 1 or x.z = 'abc'    |
| not                   | yes       | `not(<FIELD_OPERATOR>)`                         | not(exists(x.y) = false)  |
| nor                   | yes       | `nor`                                           | x.y = 1 nor x.z = 'abc'   |
| **Arrays**            |           |                                                 |                           |
| all                   | no        | `<FIELD_NAME> ALL ( <LITERAL> )`                | x.y all (1 2 3)           |
| elemMatch             | no        | `<FIELD_NAME> MATCHES ( <COMPARISION> )`        | x.y matches (a=1 and b=2) |
| size                  | yes       | `size(<FIELD_NAME>) = <INT_NUMBER>`             | size(x.y) =6              |
| **Elements**          |           |                                                 |                           |
| exists                | yes       | `exists(<FIELD_NAME>) = <BOOLEAN>`              | exists(x.y) = false       |
| type                  | yes       | `type(<FIELD_NAME>) = <STRING_LITERAL>`         | type(x.y) = 'string'      |
| **Evaluation**        |           |                                                 |                           |
| mod                   | yes       | `mod(<FIELD_NAME>,<INT_NUMBER>) = <INT_NUMBER>` | mod(x.y,2) = 1            |
| regex                 | yes       | `<FIELD_NAME> like <STRING_LITERAL>`            | x.y like '.*abc'          |
| text                  | no        |                                                 |                           |
| where                 | no        |                                                 |                           |
| **Bitwise**           |           |                                                 |                           |
| bitsAllSet            | no        |                                                 |                           |
| bitsAllClear          | no        |                                                 |                           |
| bitsAnySet            | no        |                                                 |                           |
| bitsAnyClear          | no        |                                                 |                           |
| **Geospatial**        |           |                                                 |                           |
| geoWithin             | no        |                                                 |                           |
| geoWithinBox          | no        |                                                 |                           |
| geoWithinPolygon      | no        |                                                 |                           |
| geoWithinCenter       | no        |                                                 |                           |
| geoWithinCenterSphere | no        |                                                 |                           |
| geoIntersects         | no        |                                                 |                           |
| near                  | no        |                                                 |                           |
| nearSphere            | no        |                                                 |                           |

#### Field type
You can traverse through document nested fields using standard MongoDB dot notation. 
Any character chain not enclosed in '' is considered field declaration (apart from types mentioned below).

For example: `field1.subField2` means to compare field called subField2 in object field1.

#### Comparision Types
Following types are supported for comparision:
* **Strings** - any character chain enclosed in `''` (e.g.: `field = 'this is string'`)
* **Decimal numbers** - any digits chain separated by `.` (e.g.: `field = 9.123`)
* **Integers**  - any digits chain without decimal separator (e.g.: `field = 9`)
* **Dates** - `YYYY-MM-DD` notation
* **Dates with time** - `YYYY-MM-DDTHH:MI:SS` notation
* **Spring special token** - `?<DIGIT>` - token used to map funtion parameters in Spring Query interface (e.g. `field = ?0`).
This token is valid only with Spring. In other cases it is treated as string literal.


### Important notes
1. As of now, there is no support for escaping `'` character in string literals. May be added in future releases.
1. All integers with negative sign are by default transformed to double type.
1. All dates are by default transformed to timestamp e.g. 2020-01-01 is transformed to 2020-01-01T00:00:00.   
1. **<ins>Engine does not do any optimisations of query written by user.</ins>** In case of simple queries it might not have any difference, but for very large and complex queries (many nestings of and/or operators) there might be a difference in response times. 
Please check impact on your querry resolution time, before applying to production environment.

# Bug tracker
Issues and pull requests are administered at [GitHub](https://github.com/pwszpl/mongodb-search-query-translator).
