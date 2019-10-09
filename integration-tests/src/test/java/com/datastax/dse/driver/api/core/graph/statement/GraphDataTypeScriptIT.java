/*
 * Copyright DataStax, Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.dse.driver.api.core.graph.statement;

import com.datastax.dse.driver.api.core.DseSession;
import com.datastax.dse.driver.api.core.graph.GraphDataTypeITBase;
import com.datastax.dse.driver.api.core.graph.SampleGraphScripts;
import com.datastax.dse.driver.api.core.graph.ScriptGraphStatement;
import com.datastax.dse.driver.api.testinfra.session.DseSessionRuleBuilder;
import com.datastax.oss.driver.api.testinfra.DseRequirement;
import com.datastax.oss.driver.api.testinfra.ccm.CustomCcmRule;
import com.datastax.oss.driver.api.testinfra.session.SessionRule;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

@DseRequirement(min = "5.0.4", description = "DSE 5.0.4 required for script API with GraphSON 2")
public class GraphDataTypeScriptIT extends GraphDataTypeITBase {

  private static CustomCcmRule ccmRule =
      CustomCcmRule.builder()
          .withDseWorkloads("graph")
          .withDseConfiguration(
              "graph.gremlin_server.scriptEngines.gremlin-groovy.config.sandbox_enabled", "false")
          .build();

  private static SessionRule<DseSession> sessionRule =
      new DseSessionRuleBuilder(ccmRule).withCreateGraph().build();

  @ClassRule public static TestRule chain = RuleChain.outerRule(ccmRule).around(sessionRule);

  @BeforeClass
  public static void setupSchema() {
    sessionRule.session().execute(ScriptGraphStatement.newInstance(SampleGraphScripts.ALLOW_SCANS));
    sessionRule.session().execute(ScriptGraphStatement.newInstance(SampleGraphScripts.MAKE_STRICT));
  }

  @Override
  public DseSession session() {
    return sessionRule.session();
  }

  @Override
  public Vertex insertVertexAndReturn(String vertexLabel, String propertyName, Object value) {
    return sessionRule
        .session()
        .execute(
            ScriptGraphStatement.builder("g.addV(labelP).property(nameP, valueP)")
                .setQueryParam("labelP", vertexLabel)
                .setQueryParam("nameP", propertyName)
                .setQueryParam("valueP", value)
                .build())
        .one()
        .asVertex();
  }
}
