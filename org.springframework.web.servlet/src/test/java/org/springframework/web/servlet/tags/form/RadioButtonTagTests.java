/*
 * Copyright 2002-2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.web.servlet.tags.form;

import java.beans.PropertyEditorSupport;
import java.io.StringReader;
import java.text.ParseException;
import java.util.Collections;
import java.util.Locale;

import javax.servlet.jsp.tagext.Tag;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import org.springframework.beans.Pet;
import org.springframework.model.ui.format.Formatter;
import org.springframework.model.ui.support.DefaultPresentationModel;
import org.springframework.model.ui.support.FormatterRegistry;
import org.springframework.model.ui.support.GenericFormatterRegistry;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

/**
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @author Jeremy Grelle
 */
public class RadioButtonTagTests extends AbstractFormTagTests {

	private RadioButtonTag tag;

	private TestBean bean;
	
	private FormatterRegistry formatterRegistry;

	protected void onSetUp() {
		this.tag = new RadioButtonTag() {
			protected TagWriter createTagWriter() {
				return new TagWriter(getWriter());
			}
		};
		this.tag.setPageContext(getPageContext());
		
		DefaultPresentationModel presentationModel = new DefaultPresentationModel(this.bean);
        this.formatterRegistry = new GenericFormatterRegistry();
        presentationModel.setFormatterRegistry(this.formatterRegistry);
        
        this.tag.setPresentationModel(presentationModel);     
        this.tag.setLegacyBinding(false);
	}

	public void testWithCheckedValue() throws Exception {
		this.tag.setPath("sex");
		this.tag.setValue("M");
		int result = this.tag.doStartTag();
		assertEquals(Tag.SKIP_BODY, result);

		String output = getOutput();
		assertTagOpened(output);
		assertTagClosed(output);
		assertContainsAttribute(output, "name", "sex");
		assertContainsAttribute(output, "type", "radio");
		assertContainsAttribute(output, "value", "M");
		assertContainsAttribute(output, "checked", "checked");
	}
	
	public void testWithCheckedValueLegacy() throws Exception {
	    enableLegacyBinding(this.tag);
	    testWithCheckedValue();
	}

	public void testWithCheckedObjectValue() throws Exception {
		this.tag.setPath("myFloat");
		this.tag.setValue(getFloat());
		int result = this.tag.doStartTag();
		assertEquals(Tag.SKIP_BODY, result);

		String output = getOutput();
		assertTagOpened(output);
		assertTagClosed(output);
		assertContainsAttribute(output, "name", "myFloat");
		assertContainsAttribute(output, "type", "radio");
		assertContainsAttribute(output, "value", getFloat().toString());
		assertContainsAttribute(output, "checked", "checked");
	}

	public void testWithCheckedObjectValueLegacy() throws Exception {
	    enableLegacyBinding(this.tag);
	    testWithCheckedObjectValue();
	    
	}
	
	public void testWithCheckedObjectValueAndFormatter() throws Exception {
        this.tag.setPath("myFloat");
        this.tag.setValue("F12.99");

        formatterRegistry.add(Float.class, new MyFloatFormatter());

        int result = this.tag.doStartTag();
        assertEquals(Tag.SKIP_BODY, result);

        String output = getOutput();
        assertTagOpened(output);
        assertTagClosed(output);
        assertContainsAttribute(output, "name", "myFloat");
        assertContainsAttribute(output, "type", "radio");
        assertContainsAttribute(output, "value", "F" + getFloat().toString());
        assertContainsAttribute(output, "checked", "checked");
    }
	
	public void testWithCheckedObjectValueAndEditor() throws Exception {
	    enableLegacyBinding(this.tag);
		this.tag.setPath("myFloat");
		this.tag.setValue("F12.99");

		BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(this.bean, COMMAND_NAME);
		MyFloatEditor editor = new MyFloatEditor();
		bindingResult.getPropertyEditorRegistry().registerCustomEditor(Float.class, editor);
		getPageContext().getRequest().setAttribute(BindingResult.MODEL_KEY_PREFIX + COMMAND_NAME, bindingResult);

		int result = this.tag.doStartTag();
		assertEquals(Tag.SKIP_BODY, result);

		String output = getOutput();
		assertTagOpened(output);
		assertTagClosed(output);
		assertContainsAttribute(output, "name", "myFloat");
		assertContainsAttribute(output, "type", "radio");
		assertContainsAttribute(output, "value", "F" + getFloat().toString());
		assertContainsAttribute(output, "checked", "checked");
	}

	public void testWithUncheckedObjectValue() throws Exception {
		Float value = new Float("99.45");
		this.tag.setPath("myFloat");
		this.tag.setValue(value);
		int result = this.tag.doStartTag();
		assertEquals(Tag.SKIP_BODY, result);

		String output = getOutput();
		assertTagOpened(output);
		assertTagClosed(output);
		assertContainsAttribute(output, "name", "myFloat");
		assertContainsAttribute(output, "type", "radio");
		assertContainsAttribute(output, "value", value.toString());
		assertAttributeNotPresent(output, "checked");
	}
	
	public void testWithUncheckedObjectValueLegacy() throws Exception { 
	    enableLegacyBinding(this.tag);
	    testWithUncheckedObjectValue();
	}
	

	public void testWithUncheckedValue() throws Exception {
		this.tag.setPath("sex");
		this.tag.setValue("F");
		int result = this.tag.doStartTag();
		assertEquals(Tag.SKIP_BODY, result);

		String output = getOutput();
		assertTagOpened(output);
		assertTagClosed(output);
		assertContainsAttribute(output, "name", "sex");
		assertContainsAttribute(output, "type", "radio");
		assertContainsAttribute(output, "value", "F");
		assertAttributeNotPresent(output, "checked");
	}
	
	public void testWithUncheckedValueLegacy() throws Exception {
	    enableLegacyBinding(this.tag);
	    testWithUncheckedValue();
	}

	public void testCollectionOfPets() throws Exception {
		this.tag.setPath("pets");
		this.tag.setValue(new Pet("Rudiger"));

		int result = this.tag.doStartTag();
		assertEquals(Tag.SKIP_BODY, result);

		String output = getOutput();

		// wrap the output so it is valid XML
		output = "<doc>" + output + "</doc>";

		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(output));
		Element checkboxElement = (Element) document.getRootElement().elements().get(0);
		assertEquals("input", checkboxElement.getName());
		assertEquals("radio", checkboxElement.attribute("type").getValue());
		assertEquals("pets", checkboxElement.attribute("name").getValue());
		assertEquals("Rudiger", checkboxElement.attribute("value").getValue());
		assertEquals("checked", checkboxElement.attribute("checked").getValue());
	}
	
	public void testCollectionOfPetsLegacy() throws Exception {
	    enableLegacyBinding(this.tag);
	    testCollectionOfPets();
	}

	public void testCollectionOfPetsNotSelected() throws Exception {
		this.tag.setPath("pets");
		this.tag.setValue(new Pet("Santa's Little Helper"));

		int result = this.tag.doStartTag();
		assertEquals(Tag.SKIP_BODY, result);

		String output = getOutput();

		// wrap the output so it is valid XML
		output = "<doc>" + output + "</doc>";

		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(output));
		Element checkboxElement = (Element) document.getRootElement().elements().get(0);
		assertEquals("input", checkboxElement.getName());
		assertEquals("radio", checkboxElement.attribute("type").getValue());
		assertEquals("pets", checkboxElement.attribute("name").getValue());
		assertEquals("Santa's Little Helper", checkboxElement.attribute("value").getValue());
		assertNull(checkboxElement.attribute("checked"));
	}
	
	public void testCollectionOfPetsNotSelectedLegacy() throws Exception {
	    enableLegacyBinding(this.tag);
	    testCollectionOfPetsNotSelected();
	}
	
	public void testCollectionOfPetsWithFormatter() throws Exception {
        this.tag.setPath("pets");
        this.tag.setValue(new Pet("Rudiger"));

        formatterRegistry.add(Pet.class, new MyPetFormatter());

        int result = this.tag.doStartTag();
        assertEquals(Tag.SKIP_BODY, result);

        String output = getOutput();

        // wrap the output so it is valid XML
        output = "<doc>" + output + "</doc>";

        SAXReader reader = new SAXReader();
        Document document = reader.read(new StringReader(output));
        Element checkboxElement = (Element) document.getRootElement().elements().get(0);
        assertEquals("input", checkboxElement.getName());
        assertEquals("radio", checkboxElement.attribute("type").getValue());
        assertEquals("pets", checkboxElement.attribute("name").getValue());
        assertEquals("Rudiger", checkboxElement.attribute("value").getValue());
        assertEquals("checked", checkboxElement.attribute("checked").getValue());
    }

	public void testCollectionOfPetsWithEditor() throws Exception {
	    enableLegacyBinding(this.tag);
		this.tag.setPath("pets");
		this.tag.setValue(new ItemPet("Rudiger"));

		BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(this.bean, COMMAND_NAME);
		PropertyEditorSupport editor = new ItemPet.CustomEditor();
		bindingResult.getPropertyEditorRegistry().registerCustomEditor(ItemPet.class, editor);
		getPageContext().getRequest().setAttribute(BindingResult.MODEL_KEY_PREFIX + COMMAND_NAME, bindingResult);

		int result = this.tag.doStartTag();
		assertEquals(Tag.SKIP_BODY, result);

		String output = getOutput();

		// wrap the output so it is valid XML
		output = "<doc>" + output + "</doc>";

		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(output));
		Element checkboxElement = (Element) document.getRootElement().elements().get(0);
		assertEquals("input", checkboxElement.getName());
		assertEquals("radio", checkboxElement.attribute("type").getValue());
		assertEquals("pets", checkboxElement.attribute("name").getValue());
		assertEquals("Rudiger", checkboxElement.attribute("value").getValue());
		assertEquals("checked", checkboxElement.attribute("checked").getValue());
	}

	private void assertTagOpened(String output) {
		assertTrue(output.indexOf("<input ") > -1);
	}

	private void assertTagClosed(String output) {
		assertTrue(output.indexOf("/>") > -1);
	}

	private Float getFloat() {
		return new Float("12.99");
	}

	protected TestBean createTestBean() {
		this.bean = new TestBean();
		bean.setSex("M");
		bean.setMyFloat(getFloat());
		bean.setPets(Collections.singletonList(new Pet("Rudiger")));
		return bean;
	}


	private static class MyFloatEditor extends PropertyEditorSupport {

		public void setAsText(String text) throws IllegalArgumentException {
			setValue(text.substring(1));
		}

		public String getAsText() {
			return "F" + (Float) getValue();
		}
	}
	
	private static class MyFloatFormatter implements Formatter<Float> {

        @Override
        public String format(Float object, Locale locale) {
            return "F"+object.toString();
        }

        @Override
        public Float parse(String formatted, Locale locale) throws ParseException {
            return new Float(formatted.substring(1));
        }	    
	}
	
	public static class MyPetFormatter implements Formatter<Pet> {

        @Override
        public String format(Pet object, Locale locale) {
            return object.getName();
        }

        @Override
        public Pet parse(String formatted, Locale locale) throws ParseException {
            return new Pet(formatted);
        } 
    }

}
