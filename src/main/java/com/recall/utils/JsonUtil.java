package com.recall.utils;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

@Slf4j
public class JsonUtil {
    /** Shared ObjectMapper instance for JSON processing. */
    public static ObjectMapper objectMapper = new ObjectMapper();

    static {
        // Disable default timestamp format for dates
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        // Set timezone to China Shanghai (GMT+8)
        objectMapper.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        // Do not serialize null values
        objectMapper.setSerializationInclusion(Include.NON_NULL);
        // Handle unknown properties during deserialization
        objectMapper.getDeserializationConfig().withoutFeatures(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, Boolean.TRUE);
        // Allow single quotes in JSON
        objectMapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        // Accept case-insensitive property names
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);

        // Configure LocalDateTime serialization
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        JavaTimeModule javaTimeModule = new JavaTimeModule();

        // Date serializers
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern("HH:mm:ss")));

        // Date deserializers
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern("HH:mm:ss")));

        objectMapper.registerModule(javaTimeModule);
    }

    /**
     * Converts a JSON string to an object of the specified class.
     * @param json The JSON string
     * @param clazz The class of the object
     * @param <T> The type of the object
     * @return The deserialized object, or null if an error occurs
     */
    public static <T> T toObject(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Converts an object to a JSON string.
     * @param entity The object to serialize
     * @param <T> The type of the object
     * @return The JSON string, or null if an error occurs
     */
    public static <T> String toJson(T entity) {
        try {
            return objectMapper.writeValueAsString(entity);
        } catch (Exception e) {
            log.error("toJson error:{}", e.getMessage(), e);
        }
        return null;
    }

    /**
     * Converts a JSON array string to a list of objects.
     * @param jsonArrayStr The JSON array string
     * @param clazz The class of the objects in the list
     * @param <T> The type of the objects
     * @return A list of deserialized objects
     * @throws Exception If an error occurs during deserialization
     */
    public static <T> List<T> json2list(String jsonArrayStr, Class<T> clazz)
            throws Exception {
        List<Map<String, Object>> list = (List<Map<String, Object>>) objectMapper.readValue(jsonArrayStr,
                new TypeReference<List<T>>() {
                });
        List<T> result = new ArrayList<>();
        for (Map<String, Object> map : list) {
            result.add(map2pojo(map, clazz));
        }
        return result;
    }

    /**
     * Converts a Map to a Plain Old Java Object (POJO).
     * @param map The map to convert
     * @param clazz The class of the POJO
     * @param <T> The type of the POJO
     * @return The deserialized POJO
     */
    public static <T> T map2pojo(Map map, Class<T> clazz) {
        return objectMapper.convertValue(map, clazz);
    }


    /**
     * Converts an object to a JSON string (compact format).
     * @param obj The object to serialize
     * @param <T> The type of the object
     * @return The JSON string, or null if an error occurs
     */
    public static <T> String obj2String(T obj){
        if(obj == null){
            return null;
        }
        try {
            return obj instanceof String ? (String)obj :  objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("bean转换为json串异常,原因;{}",e.getMessage(),e);
            return null;
        }
    }

    /**
     * Converts an object to a pretty-printed JSON string (formatted for debugging).
     * @param obj The object to serialize
     * @param <T> The type of the object
     * @return The formatted JSON string, or null if an error occurs
     */
    public static <T> String obj2StringPretty(T obj){
        if(obj == null){
            return null;
        }
        try {
            return obj instanceof String ? (String)obj :  objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Exception e) {
            log.error("bean转换为json串异常,原因;{}",e.getMessage(),e);
            return null;
        }
    }

    /**
     * Converts a JSON string to an object.
     * @param str The JSON string
     * @param clazz The class of the object
     * @param <T> The type of the object
     * @return The deserialized object, or null if an error occurs
     */
    public static <T> T string2Obj(String str,Class<T> clazz){
        if(!StringUtils.hasLength(str) || clazz == null){
            return null;
        }

        try {
            return clazz.equals(String.class)? (T)str : objectMapper.readValue(str,clazz);
        } catch (Exception e) {
            log.error("JsonUtil string2Obj error:{}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Converts a JSON string to a generic collection (e.g., List&lt;T&gt;).
     * @param str The JSON string
     * @param collectionClass The collection class (e.g., List.class)
     * @param elementClasses The element classes in the collection
     * @param <T> The type of the elements
     * @return The deserialized collection, or null if an error occurs
     */
    public static <T> T string2Obj(String str,Class<?> collectionClass,Class<?>... elementClasses){
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(collectionClass,elementClasses);
        try {
            return objectMapper.readValue(str,javaType);
        } catch (Exception e) {
            log.error("JsonUtil string2Obj error:{}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Converts a JSON string to an object of a generic type.
     * @param jsonStr The JSON string
     * @param targetType The target type (e.g., new TypeReference&lt;List&lt;MyClass&gt;&gt;() {})
     * @param <T> The type of the object
     * @return The deserialized object
     * @throws IllegalArgumentException If an error occurs during deserialization
     */
    public static <T> T json2obj(String jsonStr, Type targetType) {
        try {
            JavaType javaType = TypeFactory.defaultInstance().constructType(targetType);
            return objectMapper.readValue(jsonStr, javaType);
        } catch (Exception e) {
            throw new IllegalArgumentException("将JSON转换为对象时发生错误:" + jsonStr, e);
        }
    }

}
