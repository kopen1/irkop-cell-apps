package com.irkop.cell

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.UUID

class ApiException(val status:Int,val code:String,override val message:String):Exception(message)

class ApiClient(context:Context){
    private val prefs=context.getSharedPreferences("irkop_cell",Context.MODE_PRIVATE)
    private val base="https://konter.irkop.workers.dev"
    var token:String? get()=prefs.getString("token",null) private set(v){prefs.edit().putString("token",v).apply()}
    fun hasToken()=!token.isNullOrBlank()
    fun clearToken(){prefs.edit().remove("token").remove("user").apply()}
    suspend fun login(username:String,password:String):JSONObject{val r=request("POST","/api/auth/login",JSONObject().put("username",username).put("password",password),false);token=r.optString("token");prefs.edit().putString("user",r.optJSONObject("user")?.toString()).apply();return r}
    suspend fun logout(){runCatching{request("POST","/api/auth/logout",null,true)};clearToken()}
    suspend fun get(path:String,params:Map<String,String> = emptyMap())=request("GET",path,null,true,params,false)
    suspend fun post(path:String,body:JSONObject,financial:Boolean=false)=request("POST",path,body,true,emptyMap(),financial)
    suspend fun put(path:String,body:JSONObject,financial:Boolean=false)=request("PUT",path,body,true,emptyMap(),financial)
    suspend fun delete(path:String,body:JSONObject?=null,financial:Boolean=true)=request("DELETE",path,body,true,emptyMap(),financial)
    private suspend fun request(method:String,path:String,body:JSONObject?,auth:Boolean,params:Map<String,String> = emptyMap(),financial:Boolean):JSONObject=withContext(Dispatchers.IO){
        val q=params.filterValues{it.isNotBlank()}.entries.joinToString("&"){URLEncoder.encode(it.key,"UTF-8")+"="+URLEncoder.encode(it.value,"UTF-8")}
        val c=(URL(base+path+if(q.isBlank())"" else "?$q").openConnection() as HttpURLConnection).apply{requestMethod=method;connectTimeout=15000;readTimeout=20000;setRequestProperty("Accept","application/json");if(auth&&!token.isNullOrBlank())setRequestProperty("Authorization","Bearer $token");if(body!=null){doOutput=true;setRequestProperty("Content-Type","application/json")};if(financial)setRequestProperty("Idempotency-Key","ir-${UUID.randomUUID()}")}
        try{if(body!=null)c.outputStream.use{it.write(body.toString().toByteArray())};val s=c.responseCode;val text=(if(s in 200..299)c.inputStream else c.errorStream)?.bufferedReader()?.use{it.readText()}.orEmpty();val r=runCatching{JSONObject(text)}.getOrElse{JSONObject()};if(s !in 200..299){val e=r.optJSONObject("error")?:JSONObject();if(s==401)clearToken();throw ApiException(s,e.optString("code","http_$s"),e.optString("message","Terjadi kesalahan pada server."))};r}finally{c.disconnect()}
    }
}
