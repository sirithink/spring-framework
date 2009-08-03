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

import java.beans.PropertyEditor;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.Tag;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockPageContext;
import org.springframework.model.ui.support.DefaultPresentationModel;
import org.springframework.model.ui.support.FormatterRegistry;
import org.springframework.model.ui.support.GenericFormatterRegistry;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.support.BindStatus;
import org.springframework.web.servlet.support.RequestContext;
import org.springframework.web.servlet.tags.RequestContextAwareTag;

/**
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @author Scott Andrews
 * @author Jeremy Grelle
 */
public final class OptionsTagTests extends AbstractFormTagTests {

	private static final String COMMAND_NAME = "testBean";

	private SelectTag selectTag;
	private OptionsTag tag;

    private FormatterRegistry formatterRegistry;
    private DefaultPresentationModel presentationModel;
    private TestBean bean;

	protected void onSetUp() {
		this.tag = new OptionsTag() {
			protected TagWriter createTagWriter() {
				return new TagWriter(getWriter());
			}
		};
		selectTag = new SelectTag() {
			protected TagWriter createTagWriter() {
				return new TagWriter(getWriter());
			}
		};
		this.selectTag.setPageContext(getPageContext());
		this.tag.setParent(selectTag);
		this.tag.setPageContext(getPageContext());
		
		this.presentationModel = new DefaultPresentationModel(this.bean);
		this.formatterRegistry = new GenericFormatterRegistry();
        this.presentationModel.setFormatterRegistry(this.formatterRegistry);
		this.selectTag.setPresentationModel(this.presentationModel);
		
		this.tag.setLegacyBinding(false);
		this.selectTag.setLegacyBinding(false);
    }
    
    private void setupPropertyBinding(String property) {
        if (this.tag.isLegacyBinding()) {
            getPageContext().setAttribute(SelectTag.LIST_VALUE_PAGE_ATTRIBUTE, new BindStatus(getRequestContext(), property, false));
        } else {
            getPageContext().setAttribute(SelectTag.LIST_VALUE_PAGE_ATTRIBUTE, this.presentationModel.getFieldModel(property.replaceFirst("testBean\\.", "")));
        }
    }

	public void testWithCollection() throws Exception {
	    setupPropertyBinding("testBean.country");

		this.tag.setItems("${countries}");
		this.tag.setItemValue("isoCode");
		this.tag.setItemLabel("name");
		this.tag.setId("myOption");
		this.tag.setCssClass("myClass");
		this.tag.setOnclick("CLICK");
		int result = this.tag.doStartTag();
		assertEquals(Tag.SKIP_BODY, result);
		String output = getOutput();
		output = "<doc>" + output + "</doc>";

		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(output));
		Element rootElement = document.getRootElement();

		List children = rootElement.elements();
		assertEquals("Incorrect number of children", 4, children.size());

		Element element = (Element) rootElement.selectSingleNode("option[@value = 'UK']");
		assertEquals("UK node not selected", "selected", element.attribute("selected").getValue());
		assertEquals("myOption3", element.attribute("id").getValue());
		assertEquals("myClass", element.attribute("class").getValue());
		assertEquals("CLICK", element.attribute("onclick").getValue());
	}
	
	public void testWithCollectionLegacy() throws Exception {
	    enableLegacyBinding(this.tag);
	    enableLegacyBinding(this.selectTag);
	    testWithCollection();
	}

	public void testWithCollectionAndCustomFormatter() throws Exception {
        TestBean target = new TestBean();
        target.setMyFloat(new Float("12.34"));
        setupPropertyBinding("testBean.myFloat");
        
        this.formatterRegistry.add(Float.class, new SimpleFloatFormatter());

        this.tag.setItems("${floats}");
        int result = this.tag.doStartTag();
        assertEquals(Tag.SKIP_BODY, result);
        String output = getOutput();
        output = "<doc>" + output + "</doc>";

        SAXReader reader = new SAXReader();
        Document document = reader.read(new StringReader(output));
        Element rootElement = document.getRootElement();

        List children = rootElement.elements();
        assertEquals("Incorrect number of children", 6, children.size());

        Element element = (Element) rootElement.selectSingleNode("option[text() = '12.34f']");
        assertNotNull("Option node should not be null", element);
        assertEquals("12.34 node not selected", "selected", element.attribute("selected").getValue());
        assertNull("No id rendered", element.attribute("id"));

        element = (Element) rootElement.selectSingleNode("option[text() = '12.35f']");
        assertNotNull("Option node should not be null", element);
        assertNull("12.35 node incorrectly selected", element.attribute("selected"));
        assertNull("No id rendered", element.attribute("id"));
    }
	
	public void testWithCollectionAndCustomEditor() throws Exception {
	    enableLegacyBinding(this.tag);
	    enableLegacyBinding(this.selectTag);
	    
		PropertyEditor propertyEditor = new SimpleFloatEditor();

		TestBean target = new TestBean();
		target.setMyFloat(new Float("12.34"));

		BeanPropertyBindingResult errors = new BeanPropertyBindingResult(target, COMMAND_NAME);
		errors.getPropertyAccessor().registerCustomEditor(Float.class, propertyEditor);
		exposeBindingResult(errors);

		setupPropertyBinding("testBean.myFloat");

		this.tag.setItems("${floats}");
		int result = this.tag.doStartTag();
		assertEquals(Tag.SKIP_BODY, result);
		String output = getOutput();
		output = "<doc>" + output + "</doc>";

		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(output));
		Element rootElement = document.getRootElement();

		List children = rootElement.elements();
		assertEquals("Incorrect number of children", 6, children.size());

		Element element = (Element) rootElement.selectSingleNode("option[text() = '12.34f']");
		assertNotNull("Option node should not be null", element);
		assertEquals("12.34 node not selected", "selected", element.attribute("selected").getValue());
		assertNull("No id rendered", element.attribute("id"));

		element = (Element) rootElement.selectSingleNode("option[text() = '12.35f']");
		assertNotNull("Option node should not be null", element);
		assertNull("12.35 node incorrectly selected", element.attribute("selected"));
		assertNull("No id rendered", element.attribute("id"));
	}

	public void testWithItemsNullReference() throws Exception {
		getPageContext().getRequest().removeAttribute("countries");
		setupPropertyBinding("testBean.country");

		this.tag.setItems("${countries}");
		this.tag.setItemValue("isoCode");
		this.tag.setItemLabel("name");
		int result = this.tag.doStartTag();
		assertEquals(Tag.SKIP_BODY, result);
		String output = getOutput();
		output = "<doc>" + output + "</doc>";

		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(output));
		Element rootElement = document.getRootElement();

		List children = rootElement.elements();
		assertEquals("Incorrect number of children", 0, children.size());
	}
	
	public void testWithItemsNullReferenceLegacy() throws Exception {
	    enableLegacyBinding(selectTag);
	    enableLegacyBinding(tag);
	    testWithItemsNullReference();
	}

	public void testWithoutItems() throws Exception {
		this.tag.setItemValue("isoCode");
		this.tag.setItemLabel("name");
		this.selectTag.setPath("spouse");
		
		this.selectTag.doStartTag();
		int result = this.tag.doStartTag();
		assertEquals(Tag.SKIP_BODY, result);
		this.tag.doEndTag();
		this.selectTag.doEndTag();
		
		String output = getOutput();
		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(output));
		Element rootElement = document.getRootElement();
		
		List children = rootElement.elements();
		assertEquals("Incorrect number of children", 0, children.size());
	}
	
	public void testWithoutItemsLegacy() throws Exception {
	    enableLegacyBinding(selectTag);
	    enableLegacyBinding(tag);
	    testWithoutItems();
	}

	public void testWithoutItemsEnumParent() throws Exception {
		this.selectTag.setPath("testEnum");

		this.selectTag.doStartTag();
		int result = this.tag.doStartTag();
		assertEquals(BodyTag.SKIP_BODY, result);
		result = this.tag.doEndTag();
		assertEquals(Tag.EVAL_PAGE, result);
		this.selectTag.doEndTag();

		String output = getWriter().toString();
		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(output));
		Element rootElement = document.getRootElement();
		
		assertEquals(2, rootElement.elements().size());
		Node value1 = rootElement.selectSingleNode("option[@value = 'VALUE_1']");
		Node value2 = rootElement.selectSingleNode("option[@value = 'VALUE_2']");
		assertEquals("TestEnum: VALUE_1", value1.getText());
		assertEquals("TestEnum: VALUE_2", value2.getText());
		assertEquals(value2, rootElement.selectSingleNode("option[@selected]"));
	}
	
	public void testWithoutItemsEnumParentLegacy() throws Exception { 
	    enableLegacyBinding(selectTag);
	    enableLegacyBinding(tag);
	    testWithoutItemsEnumParent();
	}

	public void testWithoutItemsEnumParentWithExplicitLabelsAndValues() throws Exception {
		this.selectTag.setPath("testEnum");
		this.tag.setItemLabel("enumLabel");
		this.tag.setItemValue("enumValue");

		this.selectTag.doStartTag();
		int result = this.tag.doStartTag();
		assertEquals(BodyTag.SKIP_BODY, result);
		result = this.tag.doEndTag();
		assertEquals(Tag.EVAL_PAGE, result);
		this.selectTag.doEndTag();

		String output = getWriter().toString();
		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(output));
		Element rootElement = document.getRootElement();
		
		assertEquals(2, rootElement.elements().size());
		Node value1 = rootElement.selectSingleNode("option[@value = 'Value: VALUE_1']");
		Node value2 = rootElement.selectSingleNode("option[@value = 'Value: VALUE_2']");
		assertEquals("Label: VALUE_1", value1.getText());
		assertEquals("Label: VALUE_2", value2.getText());
		assertEquals(value2, rootElement.selectSingleNode("option[@selected]"));
	}
	
	public void testWithoutItemsEnumParentWithExplicitLabelsAndValuesLegacy() throws Exception {
	    enableLegacyBinding(selectTag);
	    enableLegacyBinding(tag);
	    testWithoutItemsEnumParentWithExplicitLabelsAndValues();
	}

	protected void extendRequest(MockHttpServletRequest request) {
	    super.extendRequest(request);
		request.setAttribute("countries", Country.getCountries());

		List<Float> floats = new ArrayList<Float>();
		floats.add(new Float("12.30"));
		floats.add(new Float("12.31"));
		floats.add(new Float("12.32"));
		floats.add(new Float("12.33"));
		floats.add(new Float("12.34"));
		floats.add(new Float("12.35"));

		request.setAttribute("floats", floats);
	}

	protected void exposeBindingResult(Errors errors) {
		// wrap errors in a Model
		Map model = new HashMap();
		model.put(BindingResult.MODEL_KEY_PREFIX + COMMAND_NAME, errors);

		// replace the request context with one containing the errors
		MockPageContext pageContext = getPageContext();
		RequestContext context = new RequestContext((HttpServletRequest) pageContext.getRequest(), model);
		pageContext.setAttribute(RequestContextAwareTag.REQUEST_CONTEXT_PAGE_ATTRIBUTE, context);
	}

    @Override
    protected TestBean createTestBean() {
        bean = new TestBean();
        bean.setName("foo");
        bean.setCountry("UK");
        bean.setMyFloat(new Float("12.34"));
        bean.setTestEnum(TestEnum.VALUE_2);
        return bean;
    }

}
