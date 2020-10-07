package no.kristiania;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class QueryString {
    private final Map<String, String> parameters = new LinkedHashMap<>();


    public QueryString(String queryString) {
        if (queryString.isEmpty()) return;
        for (String parameter : queryString.split("&")){
            int equalPos = parameter.indexOf('=');
            String parameterName = parameter.substring(0, equalPos);
            String value = parameter.substring(equalPos+1);
            this.parameters.put(parameterName, value);
        }

    }

    public String getParameter(String key) {
        return parameters.get(key);

    }

    public String getQueryString(){
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, String> parameter : parameters.entrySet()){
            if (result.length() > 0){
                result.append("&");
            }
            result.append(parameter.getKey())
                    .append("=")
                    .append(parameter.getValue());
        }

        return result.toString();
    }

    public void addParameter(String key, String value){
        parameters.put(key, value);
    }

}

