/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package iCorel.MVC;

import java.io.*;
import java.lang.reflect.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author zax
 */
public class Instance implements Filter {

    public static HashMap<String, String> actions = new HashMap();
    private static final boolean debug = true;
    private FilterConfig filterConfig = null;

    public Instance() {
    }

    private void doBeforeProcessing(ServletRequest request, ServletResponse response)
            throws IOException, ServletException {
        if (debug) {
            log("Instance:DoBeforeProcessing");
        }
        if (actions.isEmpty()) {
            try {
                List<Class> cls = getClasses(ClassLoader.getSystemClassLoader(), "c");
                cls.forEach(cl -> {
                    String controller = cl.getName();
                    controller = controller.substring(controller.lastIndexOf(".") + 1);
                    actions.put(controller.toLowerCase(), controller);
//                    Method[] ms = cl.getMethods();
//                    for (Method m : ms) {
//                        if ("iCorel.MVC.ActionResouce".equals(m.getReturnType().getName())) {
//                            String url = controller + "/" + m.getName();
//                            iCorel.MVC.Instance.actions.put(url.toLowerCase(), url);
//                        }
//                    }
                });
            } catch (Exception ex) {
                Logger.getLogger(Instance.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    private void doAfterProcessing(ServletRequest request, ServletResponse response)
            throws IOException, ServletException {
        if (debug) {
            log("Instance:DoAfterProcessing");
        }

	// Write code here to process the request and/or response after
        // the rest of the filter chain is invoked.
        // For example, a logging filter might log the attributes on the
        // request object after the request has been processed. 
	/*
         for (Enumeration en = request.getAttributeNames(); en.hasMoreElements(); ) {
         String name = (String)en.nextElement();
         Object value = request.getAttribute(name);
         log("attribute: " + name + "=" + value.toString());

         }
         */
        // For example, a filter might append something to the response.
	/*
         PrintWriter respOut = new PrintWriter(response.getWriter());
         respOut.println("<P><B>This has been appended by an intrusive filter.</B>");
         */
    }

    /**
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @param chain The filter chain we are processing
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet error occurs
     */
    public void doFilter(ServletRequest request1, ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        if (debug) {
            log("Instance:doFilter()");
        }
        if (!(request1 instanceof HttpServletRequest)) {
            chain.doFilter(request1, response);
            return;
        }
        HttpServletRequest request = (HttpServletRequest) request1;
        doBeforeProcessing(request, response);
//        try {
//            PrintWriter wr = response.getWriter();
//            actions.forEach((x, y) -> {
//                wr.write(x + "->" + y + "\n");
//            });
//        } catch (IOException ex) {
//        }
        Throwable problem = null;
        try {
            String url = ((HttpServletRequest) request).getRequestURI();
            //response.getWriter().write(url);
            if (url.startsWith("/")) {
                url = url.substring(1);
            }
            if (url.startsWith("/")) {
                url = url.substring(1);
            }
            if (url.contains("?")) {
                url = url.substring(0, url.indexOf("?"));
            }
            String[] strs = url.split("/");
            String strCtrl = "home";
            String strAct = "index";
            if (strs.length >= 1 && !"".equals(strs[0]) && strs[0] != null) {
                strCtrl = strs[0];
            }
            if (strs.length >= 2 && !"".equals(strs[1]) && strs[1] != null) {
                strAct = strs[1];
            }
            strCtrl = actions.get(strCtrl.toLowerCase());
            Class clsCtrl = Class.forName("c." + strCtrl);
            Controller ctrl = (Controller) clsCtrl.getConstructor().newInstance();
            //ctrl.viewName = +subUrl;
            ctrl.request = request;
            ctrl.response = response;
            //p.getType().getMethod("parse",new Class[]{String.class}).invoke(p, ms);
            Method[] ms = clsCtrl.getMethods();
            Method act = null;
            int max = -1;
            Set<String> ps = request.getParameterMap().keySet();
            //response.getWriter().write("Test");
            for (Method m : ms) {
                if (m.getName().equalsIgnoreCase(strAct) && m.getParameterCount() > max) {
                    String[] pns = new String[0];
                    if (m.getAnnotation(Params.class) != null) {
                        pns = m.getAnnotation(Params.class).value();
                    }
                    if (ps.containsAll(Arrays.asList(pns))) {
                        max = m.getParameterCount();
                        act = m;
                    }
                    //response.getWriter().write(String.join(",", pns));
                }
            }

            //response.getWriter().write(act.getName());
            ArrayList<Object> params = new ArrayList<>();
            String[] pns = new String[0];
            if (act.getAnnotation(Params.class) != null) {
                pns = act.getAnnotation(Params.class).value();
            }else{
                pns=request.getParameterMap().keySet().toArray(pns);
            }
            int i=0;
            for (Parameter p : act.getParameters()) {
                String pName=pns[i++];
                if (p.getType().equals(String.class)) {
                    params.add(request.getParameter(pName));
                } else if (p.getType().getMethod("valueOf", String.class) != null) {
                    params.add(p.getType().getMethod("valueOf", String.class)
                            .invoke(null, request.getParameter(pName)));
                }
            }
            ActionResouce resouce = (ActionResouce) act.invoke(ctrl, params.toArray());
            resouce.fixViewPart(strCtrl, strAct);
            resouce.doResponse();
            //chain.doFilter(request, response);
        } catch (Throwable t) {

            //request.getRequestDispatcher("").forward(request, response);
            problem = t;
            t.printStackTrace();
        }

        doAfterProcessing(request, response);

        // If there was a problem, we want to rethrow it if it is
        // a known type, otherwise log it.
        if (problem
                != null) {
            if (problem instanceof ServletException) {
                throw (ServletException) problem;
            }
            if (problem instanceof IOException) {
                throw (IOException) problem;
            }
            sendProcessingError(problem, response);
        }
    }

    /**
     * Return the filter configuration object for this filter.
     */
    public FilterConfig getFilterConfig() {
        return (this.filterConfig);
    }

    /**
     * Set the filter configuration object for this filter.
     *
     * @param filterConfig The filter configuration object
     */
    public void setFilterConfig(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
    }

    /**
     * Destroy method for this filter
     */
    public void destroy() {
    }

    /**
     * Init method for this filter
     */
    public void init(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
        if (filterConfig != null) {
            if (debug) {
                log("Instance:Initializing filter");
            }
        }
    }

    /**
     * Return a String representation of this object.
     */
    @Override
    public String toString() {
        if (filterConfig == null) {
            return ("Instance()");
        }
        StringBuffer sb = new StringBuffer("Instance(");
        sb.append(filterConfig);
        sb.append(")");
        return (sb.toString());
    }

    private void sendProcessingError(Throwable t, ServletResponse response) {
        String stackTrace = getStackTrace(t);

        if (stackTrace != null && !stackTrace.equals("")) {
            try {
                response.setContentType("text/html");
                PrintStream ps = new PrintStream(response.getOutputStream());
                PrintWriter pw = new PrintWriter(ps);
                pw.print("<html>\n<head>\n<title>Error</title>\n</head>\n<body>\n"); //NOI18N

                // PENDING! Localize this for next official release
                pw.print("<h1>The resource did not process correctly</h1>\n<pre>\n");
                pw.print(stackTrace);
                pw.print("</pre></body>\n</html>"); //NOI18N
                pw.close();
                ps.close();
                response.getOutputStream().close();
            } catch (Exception ex) {
            }
        } else {
            try {
                PrintStream ps = new PrintStream(response.getOutputStream());
                t.printStackTrace(ps);
                ps.close();
                response.getOutputStream().close();
            } catch (Exception ex) {
            }
        }
    }

    public static String getStackTrace(Throwable t) {
        String stackTrace = null;
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            pw.close();
            sw.close();
            stackTrace = sw.getBuffer().toString();
        } catch (Exception ex) {
        }
        return stackTrace;
    }

    public void log(String msg) {
        filterConfig.getServletContext().log(msg);
    }

    public static List<Class> getClasses(ClassLoader cl, String pack) throws Exception {

        String dottedPackage = pack.replaceAll("[/]", ".");
        List<Class> classes = new ArrayList<Class>();
        URL upackage = cl.getResource(pack);

        BufferedReader dis = new BufferedReader(new InputStreamReader((InputStream) upackage.getContent()));
        String line = null;
        while ((line = dis.readLine()) != null) {
            if (line.endsWith(".class")) {
                Class cls = Class.forName(dottedPackage + "." + line.substring(0, line.lastIndexOf('.')));
                if ("iCorel.MVC.Controller".equals(cls.getSuperclass().getName())) {
                    classes.add(cls);
                }
            }
        }
        return classes;
    }
}
