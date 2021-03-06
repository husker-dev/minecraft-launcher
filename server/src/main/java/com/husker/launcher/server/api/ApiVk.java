package com.husker.launcher.server.api;

import com.husker.launcher.server.ServerMain;
import com.husker.launcher.server.core.Profile;
import com.husker.launcher.server.services.http.ApiClass;
import com.husker.launcher.server.services.http.ApiException;
import com.husker.launcher.server.services.http.SimpleJSON;
import org.json.JSONObject;

public class ApiVk extends ApiClass {

    public JSONObject getInfo(){
        return SimpleJSON.create("id", ServerMain.Settings.getVKGroupId());
    }

    public void setInfo(Profile profile){
        profile.checkStatus("Администратор");
        ServerMain.Settings.setVKGroupId(getAttribute("id"));
    }
}
