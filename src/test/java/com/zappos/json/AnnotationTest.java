package com.zappos.json;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import com.zappos.jacinda.data.JsonEnumBean;
import com.zappos.jacinda.data.JsonFormatBean;
import com.zappos.jacinda.data.JsonIgnoreBean;
import com.zappos.jacinda.data.JsonKeyBean;
import com.zappos.jacinda.data.JsonEnumBean.MyEnum;

/**
 * 
 * @author Hussachai
 *
 */
public class AnnotationTest extends AbstractBaseTest {

  @Test
  public void testJsonIgnore() {
    JsonIgnoreBean bean = new JsonIgnoreBean();
    bean.setCountMeIn("Count Me In");
    bean.setIgnoreMe("Ignore Me");
    bean.setIgnoreGetter("Ignore Getter");
    bean.setIgnoreSetter("Ignore Setter");
    
    String json = jacinda.toJson(bean);
    System.out.println(json);
    
    Assert.assertTrue(json.contains(bean.getCountMeIn()));
    Assert.assertFalse(json.contains(bean.getIgnoreMe()));
    Assert.assertFalse(json.contains(bean.getIgnoreGetter()));
    Assert.assertTrue(json.contains(bean.getIgnoreSetter()));
    
    json = "{\"countMeIn\":\"Count Me In\",\"ignoreMe\":\"Ignore Me\",\"ignoreGetter\":\"Ignore Getter\",\"ignoreSetter\":\"Ignore Setter\"}";
    
    //TODO: should @JsonIgnore affect on writer only?
    JsonIgnoreBean bean2 = jacinda.fromJson(json, JsonIgnoreBean.class);
//    Assert.assertEquals(bean.getCountMeIn(), bean2.getCountMeIn());
//    Assert.assertNotEquals(bean.getIgnoreMe(), bean2.getIgnoreMe());
//    Assert.assertNull(bean2.getIgnoreMe());
    
  }
  
  @Test
  public void testJsonKey() {
    JsonKeyBean bean = new JsonKeyBean();
    bean.setFirstName("Jason");
    bean.setLastName("Voorhees");
    bean.setNickname(new JsonKeyBean.NickName());
    
    String json = jacinda.toJson(bean);
    System.out.println(json);
    Assert.assertTrue(json.contains("first_name"));
    Assert.assertTrue(json.contains("last_name"));
    Assert.assertTrue(json.contains("nick_name"));
    Assert.assertTrue(json.contains("short_name"));
    
    JsonKeyBean bean2 = jacinda.fromJson(json, JsonKeyBean.class);
    Assert.assertEquals(bean.getFirstName(), bean2.getFirstName());
    Assert.assertEquals(bean.getLastName(), bean2.getLastName());
    Assert.assertEquals(bean.getNickname().getName(), bean2.getNickname().getName());
  }
  
  @Test
  public void testJsonFormat() {
    JsonFormatBean bean = new JsonFormatBean();
    bean.setDate(new Date());
    /* this will be replaced with fixed date vlaue in CustomDateFormatter */
    bean.setFixedDate(new Date());
    /* custom formatter can apply new format pattern */
    bean.setDate2(new Date());
    
    String json = jacinda.toJson(bean);
    System.out.println(json);
    
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    JsonFormatBean bean2 = jacinda.fromJson(json, JsonFormatBean.class);
    Assert.assertEquals(sdf.format(bean.getDate()), sdf.format(bean2.getDate()));
    Assert.assertEquals(sdf.format(bean.getDate2()), sdf.format(bean2.getDate2()));
    Assert.assertEquals(sdf.format(JsonFormatBean.CustomDateFormatter.FIXED_DATE), sdf.format(bean2.getFixedDate()));
  }
  
  @Test
  public void testJsonEnum() {
    JsonEnumBean bean = new JsonEnumBean();
    bean.setDefaultEnum(MyEnum.FIRST);
    bean.setOrdinalEnum(MyEnum.SECOND);
    bean.setStringEnum(MyEnum.THIRD);
    
    String json = jacinda.toJson(bean);
    System.out.println(json);
    
    JsonEnumBean bean2 = jacinda.fromJson(json, JsonEnumBean.class);
  }
  
  public static void main(String[] args) {
    JsonEnumBean bean = new JsonEnumBean();
    bean.setDefaultEnum(MyEnum.FIRST);
    bean.setOrdinalEnum(MyEnum.SECOND);
    bean.setStringEnum(MyEnum.THIRD);
    
    String json = jacinda.toJson(bean);
    System.out.println(json);
    
    JsonEnumBean bean2 = jacinda.fromJson(json, JsonEnumBean.class);
  }
}
