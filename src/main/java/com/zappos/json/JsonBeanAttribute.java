package com.zappos.json;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.zappos.json.annot.JsonEnum.EnumValue;
import com.zappos.json.format.ValueFormatter;

/**
 * 
 * @author Hussachai
 *
 */
public class JsonBeanAttribute {
  
  private Method method;
  
  private Field field;

  private String attributeKey;
  
  /**
   * @JsonKey
   */
  private String jsonKey;
  
  private Class<? extends ValueFormatter<?>> formatterClass;
  
  private String formatterPattern;
  
  private EnumValue enumValue;
  
  public JsonBeanAttribute(){}
  
  public JsonBeanAttribute(Method method, Field field, String attributeKey) {
    this.method = method;
    this.field = field;
    this.attributeKey = attributeKey;
    this.jsonKey = attributeKey;
  }

  public Method getMethod() {
    return method;
  }

  public JsonBeanAttribute setMethod(Method method) {
    this.method = method;
    return this;
  }

  public Field getField() {
    return field;
  }

  public JsonBeanAttribute setField(Field field) {
    this.field = field;
    return this;
  }

  public String getAttributeKey() {
    return attributeKey;
  }

  public JsonBeanAttribute setAttributeKey(String attributeKey) {
    this.attributeKey = attributeKey;
    return this;
  }

  public String getJsonKey() {
    return jsonKey;
  }

  public JsonBeanAttribute setJsonKey(String jsonKey) {
    this.jsonKey = jsonKey;
    return this;
  }

  public Class<? extends ValueFormatter<?>> getFormatterClass() {
    return formatterClass;
  }

  public JsonBeanAttribute setFormatterClass(
      Class<? extends ValueFormatter<?>> formatterClass) {
    this.formatterClass = formatterClass;
    return this;
  }

  public String getFormatterPattern() {
    return formatterPattern;
  }

  public JsonBeanAttribute setFormatterPattern(String formatterPattern) {
    this.formatterPattern = formatterPattern;
    return this;
  }

  public EnumValue getEnumValue() {
    return enumValue;
  }

  public JsonBeanAttribute setEnumValue(EnumValue enumValue) {
    this.enumValue = enumValue;
    return this;
  }
  
}