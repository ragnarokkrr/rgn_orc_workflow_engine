package ragna.wf.orc.datapipeline;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.expression.Expression;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

@ConfigurationProperties("mongodb")
@Validated
public class MongodbSourceProperties {
  /** The MongoDB collection to query */
  private String collection;

  /** The MongoDB query */
  private String query = "{ }";

  /** The SpEL expression in MongoDB query DSL style */
  private Expression queryExpression;

  /** Whether to split the query result as individual messages. */
  private boolean split = true;

  @NotEmpty(message = "Query is required")
  public String getQuery() {
    return query;
  }

  public void setQuery(String query) {
    this.query = query;
  }

  public Expression getQueryExpression() {
    return queryExpression;
  }

  public void setQueryExpression(Expression queryExpression) {
    this.queryExpression = queryExpression;
  }

  public void setCollection(String collection) {
    this.collection = collection;
  }

  @NotBlank(message = "Collection name is required")
  public String getCollection() {
    return collection;
  }

  public boolean isSplit() {
    return split;
  }

  public void setSplit(boolean split) {
    this.split = split;
  }
}
