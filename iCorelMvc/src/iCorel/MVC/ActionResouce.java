package iCorel.MVC;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class ActionResouce {

    public String layout;
    public String viewPart;
    //public Object model
    public PageContent content= new PageContent();
    public ServletRequest request;
    public ServletResponse response;

    public ActionResouce(String viewPart, Object model, ServletRequest request, ServletResponse response) {
        this.viewPart = viewPart;
        this.request = request;
        this.response = response;
    }

    public void fixViewPart(String ctrl, String act) {
        if (viewPart == null) {
            viewPart = String.format("/%s/%s.jsp", ctrl, act);
        }
        if (!viewPart.startsWith("/")) {
            viewPart = "/" + ctrl + "/" + viewPart + ".jsp";
        }
    }

    public void doResponse() {
        request.setAttribute("content", content);
        try {
            if (layout != null) {
                request.setAttribute("View", viewPart);
                request.getRequestDispatcher(layout).forward(request, response);
            } else {
                request.getRequestDispatcher(viewPart).forward(request, response);
            }
        } catch (ServletException | IOException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
