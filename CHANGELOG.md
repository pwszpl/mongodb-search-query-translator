# 0.6.0
* added support for Spring Data mapping token

# 0.5.0
* added support for DATE and DATETIME types comparision
* moved all classes to io.github.pwszpl namespace 

# 0.4.0
* added support for following functions:
  * all
  * elemMatch
* changes in tests:
  * created TestBuilder class to make tests easier to maintain
  * added tests with query execution on DB container
* moved HashMaps initialization for CriteriaTransformer and BsonTransformer to constructors instead of making static initalization

# 0.3.0
* added support for following functions:
  * mod
  * regex
  * nor
  * not
* changed lexical structure for SIZE, TYPE, EXISTS functions

# 0.2.0
* added support for following functions:
  * size
  * type
  * exists
  * in
  * nin
* remove case sensitivity for parser tokens
* simplify parser jj file
* added support for integer numbers (removed implicit conversion to float)