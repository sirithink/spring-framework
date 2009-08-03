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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.servlet.jsp.tagext.Tag;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.Colour;
import org.springframework.beans.Pet;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
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
 * 
 * TODO - Collection binding tests fail because binding.getValue() blows up for un-typed collections
 */
public class CheckboxTagTests extends AbstractFormTagTests {

	private CheckboxTag tag;

	private TestBean bean;
	
	private FormatterRegistry formatterRegistry;

	protected void onSetUp() {
		this.tag = new CheckboxTag() {
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

	public void testWithSingleValueBooleanObjectChecked() throws Exception {
		this.tag.setPath("someBoolean");
		int result = this.tag.doStartTag();
		assertEquals(Tag.SKIP_BODY, result);
		String output = getOutput();

		// wrap the output so it is valid XML
		output = "<doc>" + output + "</doc>";

		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(output));
		Element rootElement = document.getRootElement();
		assertEquals("Both tag and hidden element not rendered", 2, rootElement.elements().size());
		Element checkboxElement = (Element) rootElement.elements().get(0);
		assertEquals("input", checkboxElement.getName());
		assertEquals("checkbox", checkboxElement.attribute("type").getValue());
		assertEquals("someBoolean", checkboxElement.attribute("name").getValue());
		assertEquals("checked", checkboxElement.attribute("checked").getValue());
		assertEquals("true", checkboxElement.attribute("value").getValue());
	}
	
	public void testWithSingleValueBooleanObjectCheckedLegacy() throws Exception { 
	    enableLegacyBinding(this.tag);
	    testWithSingleValueBooleanObjectChecked();
	}

	public void testWithSingleValueBooleanChecked() throws Exception {
		this.tag.setPath("jedi");
		int result = this.tag.doStartTag();
		assertEquals(Tag.SKIP_BODY, result);
		String output = getOutput();

		// wrap the output so it is valid XML
		output = "<doc>" + output + "</doc>";

		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(output));
		Element checkboxElement = (Element) document.getRootElement().elements().get(0);
		assertEquals("input", checkboxElement.getName());
		assertEquals("checkbox", checkboxElement.attribute("type").getValue());
		assertEquals("jedi", checkboxElement.attribute("name").getValue());
		assertEquals("checked", checkboxElement.attribute("checked").getValue());
		assertEquals("true", checkboxElement.attribute("value").getValue());
	}
	
	public void testWithSingleValueBooleanCheckedLegacy() throws Exception {
	    enableLegacyBinding(this.tag);
	    testWithSingleValueBooleanChecked();
	}

	public void testWithSingleValueBooleanObjectUnchecked() throws Exception {
		this.bean.setSomeBoolean(new Boolean(false));
		this.tag.setPath("someBoolean");
		int result = this.tag.doStartTag();
		assertEquals(Tag.SKIP_BODY, result);

		String output = getOutput();

		// wrap the output so it is valid XML
		output = "<doc>" + output + "</doc>";

		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(output));
		Element checkboxElement = (Element) document.getRootElement().elements().get(0);
		assertEquals("input", checkboxElement.getName());
		assertEquals("checkbox", checkboxElement.attribute("type").getValue());
		assertEquals("someBoolean", checkboxElement.attribute("name").getValue());
		assertNull(checkboxElement.attribute("checked"));
		assertEquals("true", checkboxElement.attribute("value").getValue());
	}
	
	public void testWithSingleValueBooleanObjectUncheckedLegacy() throws Exception { 
	    enableLegacyBinding(this.tag);
	    testWithSingleValueBooleanObjectUnchecked();
	}

	public void testWithSingleValueBooleanUnchecked() throws Exception {
		this.bean.setJedi(false);
		this.tag.setPath("jedi");
		int result = this.tag.doStartTag();
		assertEquals(Tag.SKIP_BODY, result);

		String output = getOutput();

		// wrap the output so it is valid XML
		output = "<doc>" + output + "</doc>";

		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(output));
		Element checkboxElement = (Element) document.getRootElement().elements().get(0);
		assertEquals("input", checkboxElement.getName());
		assertEquals("checkbox", checkboxElement.attribute("type").getValue());
		assertEquals("jedi", checkboxElement.attribute("name").getValue());
		assertNull(checkboxElement.attribute("checked"));
		assertEquals("true", checkboxElement.attribute("value").getValue());
	}
	
	public void testWithSingleValueBooleanUncheckedLegacy() throws Exception {
	    enableLegacyBinding(this.tag);
	    testWithSingleValueBooleanUnchecked();
	}

	public void testWithSingleValueNull() throws Exception {
		this.bean.setName(null);
		this.tag.setPath("name");
		this.tag.setValue("Rob Harrop");
		int result = this.tag.doStartTag();
		assertEquals(Tag.SKIP_BODY, result);

		String output = getOutput();

		// wrap the output so it is valid XML
		output = "<doc>" + output + "</doc>";

		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(output));
		Element checkboxElement = (Element) document.getRootElement().elements().get(0);
		assertEquals("input", checkboxElement.getName());
		assertEquals("checkbox", checkboxElement.attribute("type").getValue());
		assertEquals("name", checkboxElement.attribute("name").getValue());
		assertNull(checkboxElement.attribute("checked"));
		assertEquals("Rob Harrop", checkboxElement.attribute("value").getValue());
	}
	
	public void testWithSingleValueNullLegacy() throws Exception {
	    enableLegacyBinding(this.tag);
	    testWithSingleValueNull();
	}

	public void testWithSingleValueNotNull() throws Exception {
		this.bean.setName("Rob Harrop");
		this.tag.setPath("name");
		this.tag.setValue("Rob Harrop");
		int result = this.tag.doStartTag();
		assertEquals(Tag.SKIP_BODY, result);

		String output = getOutput();

		// wrap the output so it is valid XML
		output = "<doc>" + output + "</doc>";

		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(output));
		Element checkboxElement = (Element) document.getRootElement().elements().get(0);
		assertEquals("input", checkboxElement.getName());
		assertEquals("checkbox", checkboxElement.attribute("type").getValue());
		assertEquals("name", checkboxElement.attribute("name").getValue());
		assertEquals("checked", checkboxElement.attribute("checked").getValue());
		assertEquals("Rob Harrop", checkboxElement.attribute("value").getValue());
	}
	
	public void testWithSingleValueNotNullLegacy() throws Exception {
	    enableLegacyBinding(this.tag);
	    testWithSingleValueNotNull();
	}

	/**
	 * Note - This test case highlights a difference in behavior from the old binding system, in that a formatter will be invoked whether
	 * the value to be rendered is already a String or not.
	 */
	public void testWithSingleValueAndFormatter() throws Exception {
		this.bean.setName("Rob Harrop");
        this.tag.setPath("name");
        this.tag.setValue("   Rob Harrop");
        
        this.formatterRegistry.add(String.class, new MyStringTrimmerFormatter());

        int result = this.tag.doStartTag();
        assertEquals(Tag.SKIP_BODY, result);

        String output = getOutput();

        // wrap the output so it is valid XML
        output = "<doc>" + output + "</doc>";

        SAXReader reader = new SAXReader();
        Document document = reader.read(new StringReader(output));
        Element checkboxElement = (Element) document.getRootElement().elements().get(0);
        assertEquals("input", checkboxElement.getName());
        assertEquals("checkbox", checkboxElement.attribute("type").getValue());
        assertEquals("name", checkboxElement.attribute("name").getValue());
        assertEquals("checked", checkboxElement.attribute("checked").getValue());
        assertEquals("   Rob Harrop", checkboxElement.attribute("value").getValue());
	}
	
	public void testWithSingleValueAndEditor() throws Exception { 
	    enableLegacyBinding(this.tag);
	    this.bean.setName("Rob Harrop");
        this.tag.setPath("name");
        this.tag.setValue("   Rob Harrop");
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(this.bean, COMMAND_NAME);
        bindingResult.getPropertyEditorRegistry().registerCustomEditor(String.class, new StringTrimmerEditor(false));
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
        assertEquals("checkbox", checkboxElement.attribute("type").getValue());
        assertEquals("name", checkboxElement.attribute("name").getValue());
        assertEquals("checked", checkboxElement.attribute("checked").getValue());
        assertEquals("   Rob Harrop", checkboxElement.attribute("value").getValue());
	}

	public void testWithMultiValueChecked() throws Exception {
		this.tag.setPath("stringArray");
		this.tag.setValue("foo");
		int result = this.tag.doStartTag();
		assertEquals(Tag.SKIP_BODY, result);

		String output = getOutput();

		// wrap the output so it is valid XML
		output = "<doc>" + output + "</doc>";

		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(output));
		Element checkboxElement = (Element) document.getRootElement().elements().get(0);
		assertEquals("input", checkboxElement.getName());
		assertEquals("checkbox", checkboxElement.attribute("type").getValue());
		assertEquals("stringArray", checkboxElement.attribute("name").getValue());
		assertEquals("checked", checkboxElement.attribute("checked").getValue());
		assertEquals("foo", checkboxElement.attribute("value").getValue());
	}
	
	public void testWithMultiValueCheckedLegacy() throws Exception {
	    enableLegacyBinding(this.tag);
	    testWithMultiValueChecked();
	}

	public void testWithMultiValueUnchecked() throws Exception {
		this.tag.setPath("stringArray");
		this.tag.setValue("abc");
		int result = this.tag.doStartTag();
		assertEquals(Tag.SKIP_BODY, result);

		String output = getOutput();

		// wrap the output so it is valid XML
		output = "<doc>" + output + "</doc>";

		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(output));
		Element checkboxElement = (Element) document.getRootElement().elements().get(0);
		assertEquals("input", checkboxElement.getName());
		assertEquals("checkbox", checkboxElement.attribute("type").getValue());
		assertEquals("stringArray", checkboxElement.attribute("name").getValue());
		assertNull(checkboxElement.attribute("checked"));
		assertEquals("abc", checkboxElement.attribute("value").getValue());
	}
	
	public void testWithMultiValueUncheckedLegacy() throws Exception {
	    enableLegacyBinding(this.tag);
	    testWithMultiValueUnchecked();
	}

	public void testWithMultiValueWithFormatter() throws Exception {
		this.tag.setPath("stringArray");
        this.tag.setValue("   foo");
        
        MyStringTrimmerFormatter formatter = new MyStringTrimmerFormatter();
        this.formatterRegistry.add(String.class, formatter);    
        
        int result = this.tag.doStartTag();
        assertEquals(Tag.SKIP_BODY, result);

        String output = getOutput();

        // wrap the output so it is valid XML
        output = "<doc>" + output + "</doc>";

        SAXReader reader = new SAXReader();
        Document document = reader.read(new StringReader(output));
        Element checkboxElement = (Element) document.getRootElement().elements().get(0);
        assertEquals("input", checkboxElement.getName());
        assertEquals("checkbox", checkboxElement.attribute("type").getValue());
        assertEquals("stringArray", checkboxElement.attribute("name").getValue());
        assertEquals("checked", checkboxElement.attribute("checked").getValue());
        assertEquals("   foo", checkboxElement.attribute("value").getValue());
	}
	
	public void testWithMultiValueWithEditor() throws Exception {
	    enableLegacyBinding(this.tag);
	    this.tag.setPath("stringArray");
        this.tag.setValue("   foo");
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(this.bean, COMMAND_NAME);
        MyStringTrimmerEditor editor = new MyStringTrimmerEditor();
        bindingResult.getPropertyEditorRegistry().registerCustomEditor(String.class, editor);
        getPageContext().getRequest().setAttribute(BindingResult.MODEL_KEY_PREFIX + COMMAND_NAME, bindingResult);

        int result = this.tag.doStartTag();
        assertEquals(Tag.SKIP_BODY, result);
        assertEquals(1, editor.count);

        String output = getOutput();

        // wrap the output so it is valid XML
        output = "<doc>" + output + "</doc>";

        SAXReader reader = new SAXReader();
        Document document = reader.read(new StringReader(output));
        Element checkboxElement = (Element) document.getRootElement().elements().get(0);
        assertEquals("input", checkboxElement.getName());
        assertEquals("checkbox", checkboxElement.attribute("type").getValue());
        assertEquals("stringArray", checkboxElement.attribute("name").getValue());
        assertEquals("checked", checkboxElement.attribute("checked").getValue());
        assertEquals("   foo", checkboxElement.attribute("value").getValue());
	}

	public void testWithMultiValueIntegerWithFormatter() throws Exception {
	    this.tag.setPath("someIntegerArray");
        this.tag.setValue("1");
        
        MyIntegerFormatter formatter = new MyIntegerFormatter();
        this.formatterRegistry.add(Integer.class, formatter);

        int result = this.tag.doStartTag();
        assertEquals(Tag.SKIP_BODY, result);

        String output = getOutput();

        // wrap the output so it is valid XML
        output = "<doc>" + output + "</doc>";

        SAXReader reader = new SAXReader();
        Document document = reader.read(new StringReader(output));
        Element checkboxElement = (Element) document.getRootElement().elements().get(0);
        assertEquals("input", checkboxElement.getName());
        assertEquals("checkbox", checkboxElement.attribute("type").getValue());
        assertEquals("someIntegerArray", checkboxElement.attribute("name").getValue());
        assertEquals("checked", checkboxElement.attribute("checked").getValue());
        assertEquals("1", checkboxElement.attribute("value").getValue());
	}
	
	public void testWithMultiValueIntegerWithEditor() throws Exception {
	    enableLegacyBinding(this.tag);
		this.tag.setPath("someIntegerArray");
		this.tag.setValue("   1");
		BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(this.bean, COMMAND_NAME);
		MyIntegerEditor editor = new MyIntegerEditor();
		bindingResult.getPropertyEditorRegistry().registerCustomEditor(Integer.class, editor);
		getPageContext().getRequest().setAttribute(BindingResult.MODEL_KEY_PREFIX + COMMAND_NAME, bindingResult);

		int result = this.tag.doStartTag();
		assertEquals(Tag.SKIP_BODY, result);
		assertEquals(1, editor.count);

		String output = getOutput();

		// wrap the output so it is valid XML
		output = "<doc>" + output + "</doc>";

		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(output));
		Element checkboxElement = (Element) document.getRootElement().elements().get(0);
		assertEquals("input", checkboxElement.getName());
		assertEquals("checkbox", checkboxElement.attribute("type").getValue());
		assertEquals("someIntegerArray", checkboxElement.attribute("name").getValue());
		assertEquals("checked", checkboxElement.attribute("checked").getValue());
		assertEquals("   1", checkboxElement.attribute("value").getValue());
	}

	public void testWithCollection() throws Exception {
		this.tag.setPath("stringList");
		this.tag.setValue("foo");
		int result = this.tag.doStartTag();
		assertEquals(Tag.SKIP_BODY, result);

		String output = getOutput();

		// wrap the output so it is valid XML
		output = "<doc>" + output + "</doc>";

		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(output));
		Element checkboxElement = (Element) document.getRootElement().elements().get(0);
		assertEquals("input", checkboxElement.getName());
		assertEquals("checkbox", checkboxElement.attribute("type").getValue());
		assertEquals("stringList", checkboxElement.attribute("name").getValue());
		assertEquals("checked", checkboxElement.attribute("checked").getValue());
		assertEquals("foo", checkboxElement.attribute("value").getValue());
	}
	
	public void testWithCollectionLegacy() throws Exception {
	    enableLegacyBinding(this.tag);
	    testWithCollection();
	}

	public void testWithObjectChecked() throws Exception {
		this.tag.setPath("date");
		this.tag.setValue(getDate());

		int result = this.tag.doStartTag();
		assertEquals(Tag.SKIP_BODY, result);

		String output = getOutput();

		// wrap the output so it is valid XML
		output = "<doc>" + output + "</doc>";

		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(output));
		Element checkboxElement = (Element) document.getRootElement().elements().get(0);
		assertEquals("input", checkboxElement.getName());
		assertEquals("checkbox", checkboxElement.attribute("type").getValue());
		assertEquals("date", checkboxElement.attribute("name").getValue());
		assertEquals("checked", checkboxElement.attribute("checked").getValue());
		assertEquals(getDate().toString(), checkboxElement.attribute("value").getValue());
	}
	
	public void testWithObjectCheckedLegacy() throws Exception {
	    enableLegacyBinding(this.tag);
	    testWithObjectChecked();
	}

	public void testWithObjectUnchecked() throws Exception {
		this.tag.setPath("date");
		Date date = new Date();
		this.tag.setValue(date);

		int result = this.tag.doStartTag();
		assertEquals(Tag.SKIP_BODY, result);

		String output = getOutput();

		// wrap the output so it is valid XML
		output = "<doc>" + output + "</doc>";

		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(output));
		Element checkboxElement = (Element) document.getRootElement().elements().get(0);
		assertEquals("input", checkboxElement.getName());
		assertEquals("checkbox", checkboxElement.attribute("type").getValue());
		assertEquals("date", checkboxElement.attribute("name").getValue());
		assertNull(checkboxElement.attribute("checked"));
		assertEquals(date.toString(), checkboxElement.attribute("value").getValue());
	}
	
	public void testWithObjectUncheckedLegacy() throws Exception {
	    enableLegacyBinding(this.tag);
	    testWithObjectUnchecked();
	}

	public void testCollectionOfColoursSelected() throws Exception {
		this.tag.setPath("otherColours");
		this.tag.setValue("RED");

		int result = this.tag.doStartTag();
		assertEquals(Tag.SKIP_BODY, result);

		String output = getOutput();

		// wrap the output so it is valid XML
		output = "<doc>" + output + "</doc>";

		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(output));
		Element checkboxElement = (Element) document.getRootElement().elements().get(0);
		assertEquals("input", checkboxElement.getName());
		assertEquals("checkbox", checkboxElement.attribute("type").getValue());
		assertEquals("otherColours", checkboxElement.attribute("name").getValue());
		assertEquals("checked", checkboxElement.attribute("checked").getValue());
	}
	
	public void testCollectionOfColoursSelectedLegacy() throws Exception {
	    enableLegacyBinding(this.tag);
	    testCollectionOfColoursSelected();
	}

	public void testCollectionOfColoursNotSelected() throws Exception {
		this.tag.setPath("otherColours");
		this.tag.setValue("PURPLE");

		int result = this.tag.doStartTag();
		assertEquals(Tag.SKIP_BODY, result);

		String output = getOutput();

		// wrap the output so it is valid XML
		output = "<doc>" + output + "</doc>";

		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(output));
		Element checkboxElement = (Element) document.getRootElement().elements().get(0);
		assertEquals("input", checkboxElement.getName());
		assertEquals("checkbox", checkboxElement.attribute("type").getValue());
		assertEquals("otherColours", checkboxElement.attribute("name").getValue());
		assertNull(checkboxElement.attribute("checked"));
	}
	
	public void testCollectionOfColoursNotSelectedLegacy() throws Exception {
	    enableLegacyBinding(this.tag);
	    testCollectionOfColoursNotSelected();
	}

	public void testCollectionOfPetsAsString() throws Exception {
		this.tag.setPath("pets");
		this.tag.setValue("Spot");

		int result = this.tag.doStartTag();
		assertEquals(Tag.SKIP_BODY, result);

		String output = getOutput();

		// wrap the output so it is valid XML
		output = "<doc>" + output + "</doc>";

		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(output));
		Element checkboxElement = (Element) document.getRootElement().elements().get(0);
		assertEquals("input", checkboxElement.getName());
		assertEquals("checkbox", checkboxElement.attribute("type").getValue());
		assertEquals("pets", checkboxElement.attribute("name").getValue());
		assertEquals("checked", checkboxElement.attribute("checked").getValue());
	}
	
	public void testCollectionOfPetsAsStringLegacy() throws Exception {
	    enableLegacyBinding(this.tag);
	    testCollectionOfPetsAsString();
	}

	public void testCollectionOfPetsAsStringNotSelected() throws Exception {
		this.tag.setPath("pets");
		this.tag.setValue("Santa's Little Helper");

		int result = this.tag.doStartTag();
		assertEquals(Tag.SKIP_BODY, result);

		String output = getOutput();

		// wrap the output so it is valid XML
		output = "<doc>" + output + "</doc>";

		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(output));
		Element checkboxElement = (Element) document.getRootElement().elements().get(0);
		assertEquals("input", checkboxElement.getName());
		assertEquals("checkbox", checkboxElement.attribute("type").getValue());
		assertEquals("pets", checkboxElement.attribute("name").getValue());
		assertNull(checkboxElement.attribute("checked"));
	}
	
	public void testCollectionOfPetsAsStringNotSelectedLegacy() throws Exception { 
	    enableLegacyBinding(this.tag);
	    testCollectionOfPetsAsStringNotSelected();
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
		assertEquals("checkbox", checkboxElement.attribute("type").getValue());
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
		assertEquals("checkbox", checkboxElement.attribute("type").getValue());
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

        this.formatterRegistry.add(Pet.class, new MyPetFormatter());

        int result = this.tag.doStartTag();
        assertEquals(Tag.SKIP_BODY, result);

        String output = getOutput();

        // wrap the output so it is valid XML
        output = "<doc>" + output + "</doc>";

        SAXReader reader = new SAXReader();
        Document document = reader.read(new StringReader(output));
        Element checkboxElement = (Element) document.getRootElement().elements().get(0);
        assertEquals("input", checkboxElement.getName());
        assertEquals("checkbox", checkboxElement.attribute("type").getValue());
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
		assertEquals("checkbox", checkboxElement.attribute("type").getValue());
		assertEquals("pets", checkboxElement.attribute("name").getValue());
		assertEquals("Rudiger", checkboxElement.attribute("value").getValue());
		assertEquals("checked", checkboxElement.attribute("checked").getValue());
	}

	public void testWithNullValue() throws Exception {
		try {
			this.tag.setPath("name");
			this.tag.doStartTag();
			fail("Should not be able to render with a null value when binding to a non-boolean.");
		}
		catch (IllegalArgumentException e) {
			// success
		}
	}
	
	public void testWithNullValueLegacy() throws Exception {
	    enableLegacyBinding(this.tag);
	    testWithNullValue();
	}

	public void testHiddenElementOmittedOnDisabled() throws Exception {
		this.tag.setPath("someBoolean");
		this.tag.setDisabled("true");
		int result = this.tag.doStartTag();
		assertEquals(Tag.SKIP_BODY, result);
		String output = getOutput();

		// wrap the output so it is valid XML
		output = "<doc>" + output + "</doc>";

		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(output));
		Element rootElement = document.getRootElement();
		assertEquals("Both tag and hidden element rendered incorrectly", 1, rootElement.elements().size());
		Element checkboxElement = (Element) rootElement.elements().get(0);
		assertEquals("input", checkboxElement.getName());
		assertEquals("checkbox", checkboxElement.attribute("type").getValue());
		assertEquals("someBoolean", checkboxElement.attribute("name").getValue());
		assertEquals("checked", checkboxElement.attribute("checked").getValue());
		assertEquals("true", checkboxElement.attribute("value").getValue());
	}
	
	public void testHiddenElementOmittedOnDisabledLegacy() throws Exception {
	    enableLegacyBinding(this.tag);
	    testHiddenElementOmittedOnDisabled();
	}
	
	private Date getDate() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 10);
		cal.set(Calendar.MONTH, 10);
		cal.set(Calendar.DATE, 10);
		cal.set(Calendar.HOUR, 10);
		cal.set(Calendar.MINUTE, 10);
		cal.set(Calendar.SECOND, 10);
		return cal.getTime();
	}

	protected TestBean createTestBean() {
		List<Colour> colours = new ArrayList<Colour>();
		colours.add(Colour.BLUE);
		colours.add(Colour.RED);
		colours.add(Colour.GREEN);

		List<Pet> pets = new ArrayList<Pet>();
		pets.add(new Pet("Rudiger"));
		pets.add(new Pet("Spot"));
		pets.add(new Pet("Fluffy"));
		pets.add(new Pet("Mufty"));

		this.bean = new TestBean();
		this.bean.setDate(getDate());
		this.bean.setName("Rob Harrop");
		this.bean.setJedi(true);
		this.bean.setSomeBoolean(new Boolean(true));
		this.bean.setStringArray(new String[] {"bar", "foo"});
		this.bean.setSomeIntegerArray(new Integer[] {new Integer(2), new Integer(1)});
		this.bean.setOtherColours(colours);
		this.bean.setPets(pets);
		List<String> list = new ArrayList<String>();
		list.add("foo");
		list.add("bar");
		this.bean.setStringList(list);
		return this.bean;
	}
	
	private class MyStringTrimmerFormatter implements Formatter<String> {

	    public int count = 0;
	    
        @Override
        public String format(String object, Locale locale) {
            this.count++;
            return object.trim();
        }

        @Override
        public String parse(String formatted, Locale locale) throws ParseException {
            return formatted.trim();
        }
	    
	}

	private class MyStringTrimmerEditor extends StringTrimmerEditor {

		public int count = 0;

		public MyStringTrimmerEditor() {
			super(false);
		}

		public void setAsText(String text) {
			this.count++;
			super.setAsText(text);
		}
	}
	
	private class MyIntegerFormatter implements Formatter<Integer> {
	    
	    public int count = 0;
	    
        @Override
        public String format(Integer object, Locale locale) {
            count++;
            return object.toString();
        }

        @Override
        public Integer parse(String formatted, Locale locale) throws ParseException {
            return new Integer(formatted.trim());
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

	private class MyIntegerEditor extends PropertyEditorSupport {

		public int count = 0;

		public void setAsText(String text) {
			this.count++;
			setValue(new Integer(text.trim()));
		}
	}
}
