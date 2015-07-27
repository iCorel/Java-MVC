package iCorel.MVC;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class Controller {
    public String layout="/Shared/Layout.jsp";
    public String viewName;
    public String title="iCorel MVC";
    public Map<String,String> viewBag=new HashMap();
    public Map<String,Object> viewData=new HashMap();
    public Object model;
    public ServletRequest request;
    public ServletResponse response;
    public ActionResouce View(){
        ActionResouce Out=new ActionResouce(viewName,model,request, response);
        Out.layout=layout;
        Out.content.model=model;
        Out.content.title=title;
        Out.content.viewBag=viewBag;
        Out.content.viewData=viewData;
        return Out;
    }
    public ActionResouce View(String viewName){
        this.viewName=viewName;
        return View();
    }
    public ActionResouce View(Object model){
        this.model=model;
        return View();
    }
    public ActionResouce View(String viewName,Object model){
        this.viewName=viewName;
        this.model=model;
        return View();
    }
}
