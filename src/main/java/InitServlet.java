import java.lang.management.ManagementFactory;

import javax.management.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 * @author roland
 * @since 14/07/15
 */
public class InitServlet extends HttpServlet {


    @Override
    public void init() throws ServletException {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        try {
            server.registerMBean(new PerformanceTest(),new ObjectName("jolokia:type=performance,name=PerformanceTest"));
        } catch (InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException | MalformedObjectNameException e) {
            throw new ServletException(e);
        }
    }
}
