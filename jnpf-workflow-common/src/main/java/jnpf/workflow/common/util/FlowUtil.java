package jnpf.workflow.common.util;

public class FlowUtil {

    public static boolean isDM(String url){
        return url.startsWith("jdbc:dm");
    }
}
