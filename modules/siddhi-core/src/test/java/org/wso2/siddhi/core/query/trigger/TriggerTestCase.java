/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.siddhi.core.query.trigger;

import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.query.api.ExecutionPlan;
import org.wso2.siddhi.query.api.definition.TriggerDefinition;
import org.wso2.siddhi.query.api.exception.DuplicateDefinitionException;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;
import org.wso2.siddhi.query.api.expression.Expression;

public class TriggerTestCase {
    static final Logger log = Logger.getLogger(TriggerTestCase.class);
    private volatile int count;
    private volatile boolean eventArrived;

    @Before
    public void init() {
        count = 0;
        eventArrived = false;
    }

    @Test
    public void testQuery1() throws InterruptedException {
        log.info("testTrigger1 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();

        TriggerDefinition triggerDefinition = TriggerDefinition.id("cseEventStream").atEvery(Expression.Time.milliSec(500));

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineTrigger(triggerDefinition);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);
        executionPlanRuntime.shutdown();
    }

    @Test(expected = ExecutionPlanValidationException.class)
    public void testQuery2() throws InterruptedException {
        log.info("testTrigger2 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();

        TriggerDefinition triggerDefinition = TriggerDefinition.id("cseEventStream").atEvery(Expression.Time.milliSec(500)).at("start");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineTrigger(triggerDefinition);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        executionPlanRuntime.shutdown();
    }

    @Test(expected = DuplicateDefinitionException.class)
    public void testQuery3() throws InterruptedException {
        log.info("testTrigger3 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define trigger StockStream at 'start' ";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams);

        executionPlanRuntime.start();
        executionPlanRuntime.shutdown();
    }

    @Test
    public void testQuery4() throws InterruptedException {
        log.info("testTrigger4 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream StockStream (triggered_time long); " +
                "define trigger StockStream at 'start' ";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams);

        executionPlanRuntime.start();
        executionPlanRuntime.shutdown();
    }


    @Test
    public void testFilterQuery5() throws InterruptedException {
        log.info("testTrigger5 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String plan = "" +
                "define stream cseEventStream (symbol string, price float, volume long);" +
                "define trigger triggerStream at 'start';";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(plan);

        executionPlanRuntime.addCallback("triggerStream", new StreamCallback() {

            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                count += events.length;
                eventArrived = true;
            }
        });

        executionPlanRuntime.start();

        Thread.sleep(100);
        Assert.assertEquals(1, count);
        Assert.assertEquals(true, eventArrived);
        executionPlanRuntime.shutdown();

    }

    @Test
    public void testFilterQuery6() throws InterruptedException {
        log.info("testTrigger6 - OUT 2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String plan = "" +
                "define stream cseEventStream (symbol string, price float, volume long);" +
                "define trigger triggerStream at every 500 milliseconds ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(plan);

        executionPlanRuntime.addCallback("triggerStream", new StreamCallback() {

            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                count += events.length;
                eventArrived = true;
            }
        });

        executionPlanRuntime.start();

        Thread.sleep(1100);
        Assert.assertEquals(2, count);
        Assert.assertEquals(true, eventArrived);
        executionPlanRuntime.shutdown();

    }

    @Test
    public void testFilterQuery7() throws InterruptedException {
        log.info("testTrigger7 - OUT 2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String plan = "" +
                "define stream cseEventStream (symbol string, price float, volume long);" +
                "define trigger triggerStream at '*/1 * * * * ?' ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(plan);

        executionPlanRuntime.addCallback("triggerStream", new StreamCallback() {

            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                count += events.length;
                eventArrived = true;
            }
        });

        executionPlanRuntime.start();

        Thread.sleep(1000);
        executionPlanRuntime.shutdown();

        Assert.assertEquals(2, count);
        Assert.assertEquals(true, eventArrived);

    }

}