import java.lang.management.ManagementFactory;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.*;

import com.codahale.metrics.*;

/**
 * @author roland
 * @since 14/07/15
 */
public class PerformanceTest implements PerformanceTestMBean {

    private static Logger logger = Logger.getLogger(PerformanceTest.class.getName());; ;

    private final Timer attributeTimer;
    private final Timer sumTime;
    private final Timer queryTime;
    private final Timer mbeanTimer;

    public PerformanceTest() {
        final MetricRegistry metrics = new MetricRegistry();
        attributeTimer = metrics.timer("jmx.attributes.time.read");
        mbeanTimer = metrics.timer("jmx.attributes.time.mbean");
        sumTime = metrics.timer("jmx.attributes.time.total");
        queryTime = metrics.timer("jmx.attributes.time.query");

        final JmxReporter reporter = JmxReporter.forRegistry(metrics).build();
        reporter.start();
    }

    public void testReadAllAttributes() throws Exception {
        final Timer.Context sumContext = sumTime.time();
        try {
            doScrape();
        } finally {
            sumContext.stop();
        }
    }

    public void doScrape() throws Exception {
        MBeanServerConnection beanConn;
        beanConn = ManagementFactory.getPlatformMBeanServer();

        // Query MBean names
        Timer.Context queryContext = queryTime.time();
        Set<ObjectName> mBeanNames;

        try {
            mBeanNames = new TreeSet<ObjectName>(beanConn.queryNames(null, null));
        } finally {
            queryContext.stop();
        }
        for (ObjectName name : mBeanNames) {
            Timer.Context mbeanContext = mbeanTimer.time();
            try {
                scrapeBean(beanConn, name);
            } finally {
                mbeanContext.stop();
            }
        }
    }

    private void scrapeBean(MBeanServerConnection beanConn, ObjectName mbeanName) throws Exception {
        MBeanInfo info = beanConn.getMBeanInfo(mbeanName);
        MBeanAttributeInfo[] attrInfos = info.getAttributes();

        for (int idx = 0; idx < attrInfos.length; ++idx) {
            MBeanAttributeInfo attr = attrInfos[idx];
            if (!attr.isReadable()) {
                logScrape(mbeanName, attr, "not readable",null);
                continue;
            }

            Object value;
            final Timer.Context attrContext = attributeTimer.time();
            try {
                value = beanConn.getAttribute(mbeanName, attr.getName());
            } catch(Exception e) {
                logScrape(mbeanName, attr, "Fail: " + e,null);
                continue;
            } finally {
                attrContext.stop();
            }
            logScrape(mbeanName, attr, "process",value);
        }
    }

    // add "value" so that it doesnt get optimized away
    private static void logScrape(ObjectName name, MBeanAttributeInfo attr, String msg, Object value) {
        logger.log(Level.FINE, "scrape: '" + name + "_" + attr.getName() + "': " + msg);
    }

}
