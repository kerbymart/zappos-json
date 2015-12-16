/*
 * Copyright 2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *     http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */

package com.zappos.json;

import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.zappos.json.JsonConfig.ReaderConfig;
import com.zappos.json.JsonConfig.WriterConfig;
import com.zappos.json.format.BigDecimalFormatter;
import com.zappos.json.format.BigIntegerFormatter;
import com.zappos.json.format.JavaDateFormatter;
import com.zappos.json.format.JavaSqlDateFormatter;
import com.zappos.json.format.JavaTimeInstantFormatter;
import com.zappos.json.format.JavaTimeLocalDateFormatter;
import com.zappos.json.format.JavaTimestampFormatter;
import com.zappos.json.format.ValueFormatter;
import com.zappos.json.util.Reflections;
import com.zappos.json.util.Strings;

/**
 * 
 * @author Hussachai Puripunpinyo
 *
 */
public class ZapposJson {

  protected final Map<Class<?>, ValueFormatter<Object>> VALUE_FORMATTERS = new ConcurrentHashMap<>();

  private final static Map<String, ZapposJson> INSTANCES = new HashMap<>();

  private boolean debug = false;
  
  private JsonBeanIntrospector jsonBeanIntrospector;
  
  private JsonWriterCodeGenerator writerCodeGenerator;
  
  private JsonReaderCodeGenerator readerCodeGenerator;
  
  public final boolean[] WRITER_CONFIGS = new boolean[WriterConfig.values().length];
  
  public final boolean[] READER_CONFIGS = new boolean[ReaderConfig.values().length];
  
  protected ZapposJson() {
    
    addValueFormatter(Date.class, new JavaDateFormatter());
    addValueFormatter(java.sql.Date.class, new JavaSqlDateFormatter());
    addValueFormatter(java.sql.Timestamp.class, new JavaTimestampFormatter());
    addValueFormatter(java.math.BigInteger.class, new BigIntegerFormatter());
    addValueFormatter(java.math.BigDecimal.class, new BigDecimalFormatter());
    if (Reflections.classPresent("java.time.chrono.ChronoLocalDate")) {
      ValueFormatter<LocalDate> localDateHandler = new JavaTimeLocalDateFormatter();
      addValueFormatter(LocalDate.class, localDateHandler);
      ValueFormatter<Instant> instantHandler = new JavaTimeInstantFormatter();
      addValueFormatter(Instant.class, instantHandler);
    }
    jsonBeanIntrospector = new JsonBeanIntrospector(this);
    writerCodeGenerator = new JsonWriterCodeGenerator(this, jsonBeanIntrospector);
    readerCodeGenerator = new JsonReaderCodeGenerator(this, jsonBeanIntrospector);
  }
  
  protected ZapposJson(boolean debug) {
    this();
    this.debug = debug;
  }

  public static enum JacindaSingleton {
    INSTANCE;

    ZapposJson jacinda = new ZapposJson();

    public ZapposJson get() {
      return jacinda;
    }
  }

  public static ZapposJson getInstance() {
    return JacindaSingleton.INSTANCE.get();
  }

  public synchronized static ZapposJson getInstance(String name) {
    ZapposJson instance = INSTANCES.get(name);
    if (instance == null) {
      instance = new ZapposJson();
      INSTANCES.put(name, instance);
    }
    return instance;
  }

  public void register(Class<?>... classes) {
    for (Class<?> clazz : classes) {
      register(clazz);
    }
  }

  public void register(Class<?> clazz) {
    try{
      writerCodeGenerator.registerWriter(clazz);
      readerCodeGenerator.registerReader(clazz);
    }catch(Exception e){
      throw new JsonException(e);
    }
  }
  
  public void deregister(Class<?> clazz) {
    writerCodeGenerator.deregister(clazz);
    readerCodeGenerator.deregister(clazz);
  }

  public void deregisterAll() {
    writerCodeGenerator.deregisterAll();
    readerCodeGenerator.deregisterAll();
  }

  @SuppressWarnings("unchecked")
  public void addValueFormatter(Class<?> objectType,
      ValueFormatter<?> valueFormatter) {
    VALUE_FORMATTERS.put(objectType,
        (ValueFormatter<Object>) valueFormatter);
  }

  public void removeValueFormatter(Class<?> objectType) {
    VALUE_FORMATTERS.remove(objectType);
  }

  public ValueFormatter<Object> getValueFormatter(Class<?> objectType) {
    return VALUE_FORMATTERS.get(objectType);
  }
  
  public synchronized void configure(ReaderConfig config, boolean value){
    READER_CONFIGS[config.ordinal()] = true;
  }
  
  protected synchronized boolean is(ReaderConfig config){
    return READER_CONFIGS[config.ordinal()];
  }
  
  public synchronized void configure(WriterConfig config, boolean value){
    WRITER_CONFIGS[config.ordinal()] = true;
  }
  
  protected synchronized boolean is(WriterConfig config){
    return WRITER_CONFIGS[config.ordinal()];
  }
  
  /**
   * Try to format specified value with the formatter in a registry.
   * This method throws {@link NullPointerException} when the specified value is null.
   * @param value
   * @param defaultValue
   * @return formatted value or value.toString when there is no formatter
   * @throws NullPointerException
   */
  public String format(Object value) {
    ValueFormatter<Object> valueFormatter = VALUE_FORMATTERS.get(value.getClass());
    if (valueFormatter != null) {
      return valueFormatter.format(this, value);
    } else {
      return value.toString();
    }
  }
  
  public String toJson(Object object) {
    
    StringWriter writer = new StringWriter();
    toJson(object, writer, true);
    return writer.toString();
    
  }
  
  public void toJson(Object object, Writer writer) {
    
    toJson(object, writer, true);
    
  }
  
  protected void toJson(Object object, Writer writer, boolean basicTypeCheck) {
    
    try{
      
      if(object == null){
        
        writer.append(JsonWriter.CONST_NULL);
        
      }else{
        
        if(object instanceof String || object instanceof Character) {
          
          JsonWriter.writeString(this, object.toString(), writer);
          
        }else if(object instanceof Number) {
          
          JsonWriter.writeNumber(this, (Number)object, writer);
          
        }else if(object instanceof Enum) {
          
          JsonWriter.writeEnum(this, (Enum<?>)object, writer);
          
        }else if(object instanceof Boolean) {
          
          JsonWriter.writeBoolean(this, (Boolean)object, writer);
          
        }else if(object instanceof Iterable) {
          
          JsonWriter.writeIterable(this, (Iterable<?>) object, writer);
          
        }else if(object instanceof Map) {
          
          JsonWriter.writeMap(this, (Map<?, ?>) object, writer);
          
        }else{
          Class<?> objectType = object.getClass();
          if(objectType.isArray()){
            Class<?> componentType = objectType.getComponentType();
            if(componentType == byte.class){
              JsonWriter.writeBase64String(this, (byte[])object, writer);
            }else if(componentType == char.class){
              JsonWriter.writeArray((char[])object, writer);
            }else if(componentType == boolean.class){
              JsonWriter.writeArray((boolean[])object, writer);
            }else if(componentType == short.class){
              JsonWriter.writeArray((short[])object, writer);
            }else if(componentType == int.class){
              JsonWriter.writeArray((int[])object, writer);
            }else if(componentType == long.class){
              JsonWriter.writeArray((long[])object, writer);
            }else if(componentType == float.class){
              JsonWriter.writeArray((float[])object, writer);
            }else if(componentType == double.class){
              JsonWriter.writeArray((double[])object, writer);
            }else{
              JsonWriter.writeArray(this, (Object[]) object, writer);
            }
          }else{
            JsonWriterInvoker writerInvoker = writerCodeGenerator.getWriter(objectType);
            if(writerInvoker == null){
              writerInvoker = writerCodeGenerator.registerWriter(objectType);
            }
            writerInvoker.writeJson(object, writer);
          }
        }
      }
      
    }catch(Exception e){
      throw new JsonException(e);
    }
  }
  
  public <T> T fromJson(String json, Class<T> targetClass) {
    try{
      JsonReaderInvoker readerInvoker = readerCodeGenerator.getReader(targetClass);
      if(readerInvoker == null){
          readerInvoker = readerCodeGenerator.registerReader(targetClass);
      }
      return readerInvoker.readJson(json, targetClass);
    }catch(Exception e){
      throw new JsonException(e);
    }
  }

  public <T> T fromJson(Reader reader, Class<T> targetClass) {
    try{
      JsonReaderInvoker readerInvoker = readerCodeGenerator.getReader(targetClass);
      if(readerInvoker == null){
        readerInvoker = readerCodeGenerator.registerReader(targetClass);
      }
      return readerInvoker.readJson(reader, targetClass);
    }catch(Exception e){
      throw new JsonException(e);
    }
  }
  
  protected void debug(String pattern, Object... args) {
    if (debug) {
      System.out.println(Strings.format(pattern, args));
    }
  }
  
}
