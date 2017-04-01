package com.code.server.cardgame.utils;

import com.code.server.db.model.Record;
import com.code.server.db.model.User;
import com.code.server.db.model.UserInfo;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Created by sun on 2015/8/21.
 */
public final class JsonUtil {
    public static final ObjectMapper mapper = new ObjectMapper(); // create once, reuse

    static{

        //去掉默认的时间戳格式
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        //设置为中国
        mapper.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        //空值不序列化
//        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        //反序列化时，属性不存在的兼容处理
        mapper.getDeserializationConfig().withoutFeatures(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
      
        //序列化时，日期的统一格式
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        //单引号处理
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);


        mapper.enableDefaultTyping();


    }

    public static void main(String[] args) {
        User user = new User();
        UserInfo userInfo = new UserInfo();
        
        try {
            String s = mapper.writeValueAsString(user);

            System.out.println(s);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }



}
