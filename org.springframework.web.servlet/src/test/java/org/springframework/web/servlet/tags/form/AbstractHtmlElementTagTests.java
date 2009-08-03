/*
 * Copyright 2002-2007 the original author or authors.
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

import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;

import org.springframework.beans.Colour;
import org.springframework.beans.Pet;
import org.springframework.beans.TestBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockPageContext;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.support.JspAwareRequestContext;
import org.springframework.web.servlet.support.RequestContext;
import org.springframework.web.servlet.tags.AbstractTagTests;
import org.springframework.web.servlet.tags.RequestContextAwareTag;

/**
 * @author Rob Harrop
 * @author Juergen Hoeller
 */
public abstract class AbstractHtmlElementTagTests extends AbstractTagTests {

	public static final String COMMAND_NAME = "testBean";

	private StringWriter writer;

	private MockPageContext pageContext;


	protected final void setUp() throws Exception {
		// set up a writer for the tag content to be written to
		this.writer = new StringWriter();

		// configure the page context
		this.pageContext = createAndPopulatePageContext();

		onSetUp();
	}

	protected MockPageContext createAndPopulatePageContext() throws JspException {
		MockPageContext pageContext = createPageContext();
		MockHttpServletRequest request = (MockHttpServletRequest) pageContext.getRequest();
		RequestContext requestContext = new JspAwareRequestContext(pageContext);
		pageContext.setAttribute(RequestContextAwareTag.REQUEST_CONTEXT_PAGE_ATTRIBUTE, requestContext);
		extendRequest(request);
		extendPageContext(pageContext);
		return pageContext;
	}

	protected void extendPageContext(MockPageContext pageContext) throws JspException {
	}

	protected void extendRequest(MockHttpServletRequest request) {
	}

	protected void onSetUp() {
	}

	protected MockPageContext getPageContext() {
		return this.pageContext;
	}

	protected Writer getWriter() {
		return this.writer;
	}

	protected String getOutput() {
		return this.writer.toString();
	}

	protected final RequestContext getRequestContext() {
		return (RequestContext) getPageContext().getAttribute(RequestContextAwareTag.REQUEST_CONTEXT_PAGE_ATTRIBUTE);
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
	
	protected void enableLegacyBinding(AbstractDataBoundFormElementTag tag) {
	    tag.setLegacyBinding(true);
	}

	protected final void assertContainsAttribute(String output, String attributeName, String attributeValue) {
		String attributeString = attributeName + "=\"" + attributeValue + "\"";
		assertTrue("Expected to find attribute '" + attributeName +
				"' with value '" + attributeValue +
				"' in output + '" + output + "'",
				output.indexOf(attributeString) > -1);
	}

	protected final void assertAttributeNotPresent(String output, String attributeName) {
		assertTrue("Unexpected attribute '" + attributeName + "' in output '" + output + "'.",
				output.indexOf(attributeName + "=\"") < 0);
	}

	protected final void assertBlockTagContains(String output, String desiredContents) {
		String contents = output.substring(output.indexOf(">") + 1, output.lastIndexOf("<"));
		assertTrue("Expected to find '" + desiredContents + "' in the contents of block tag '" + output + "'",
				contents.indexOf(desiredContents) > -1);
	}
	
	protected static class TestBean {

        private Date date;

        private String name;

        private boolean jedi;

        private Boolean someBoolean;

        private String[] stringArray;

        private Integer[] someIntegerArray;

        private List<Colour> otherColours;

        private List<Pet> pets;

        private List<String> stringList;

        private Set<ItemPet> itemPets;

        private Float myFloat;

        private TestBean[] spouses;

        private String sex;

        private String country;

        private List<Country> countryList;

        private Map<String, String> someMap;
        
        private TestEnum testEnum;

        private Colour favouriteColour;

        private Float someNumber;

        private List<TestBean> friends;

        private int age;

        public TestBean() {
        }

        public TestBean(String name) {
            this.name = name;
        }

        public TestBean(TestBean spouse) {
            this.spouses = new TestBean[] { spouse };
        }

        public TestBean(String name, int age) {
            this.name=name;
            this.age=age;
        }

        public Date getDate() {
            return date;
        }

        public String getName() {
            return name;
        }

        public boolean isJedi() {
            return jedi;
        }

        public Boolean getSomeBoolean() {
            return someBoolean;
        }

        public String[] getStringArray() {
            return stringArray;
        }

        public Integer[] getSomeIntegerArray() {
            return someIntegerArray;
        }

        public List<Colour> getOtherColours() {
            return otherColours;
        }

        public List<Pet> getPets() {
            return pets;
        }

        public List<String> getStringList() {
            return stringList;
        }

        public Set<ItemPet> getItemPets() {
            return itemPets;
        }

        public Float getMyFloat() {
            return myFloat;
        }

        public TestBean getSpouse() {
            return (spouses != null ? spouses[0] : null);
        }

        public TestBean[] getSpouses() {
            return spouses;
        }

        public String getSex() {
            return sex;
        }

        public String getCountry() {
            return country;
        }

        public List<Country> getCountryList() {
            return countryList;
        }

        
        public Map<String, String> getSomeMap() {
            return someMap;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setJedi(boolean jedi) {
            this.jedi = jedi;
        }

        public void setSomeBoolean(Boolean someBoolean) {
            this.someBoolean = someBoolean;
        }

        public void setStringArray(String[] stringArray) {
            this.stringArray = stringArray;
        }

        public void setSomeIntegerArray(Integer[] someIntegerArray) {
            this.someIntegerArray = someIntegerArray;
        }

        public void setOtherColours(List<Colour> otherColours) {
            this.otherColours = otherColours;
        }

        public void setPets(List<Pet> pets) {
            this.pets = pets;
        }

        public void setStringList(List<String> stringList) {
            this.stringList = stringList;
        }

        public void setItemPets(Set<ItemPet> itemPets) {
            this.itemPets = itemPets;
        }

        public void setMyFloat(Float myFloat) {
            this.myFloat = myFloat;
        }

        public void setSpouse(TestBean spouse) {
            this.spouses = new TestBean[] { spouse };
        }

        public void setSex(String sex) {
            this.sex = sex;
            if (this.name == null) {
                this.name = sex;
            }
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public void setCountryList(List<Country> countryList) {
            this.countryList = countryList;
        }

        public void setSomeMap(Map<String, String> someMap) {
            this.someMap = someMap;            
        }
        
        public TestEnum getTestEnum() {
            return testEnum;
        }

        public void setTestEnum(TestEnum customEnum) {
            this.testEnum = customEnum;
        }
        
        public Colour getFavouriteColour() {
            return favouriteColour;
        }

        public void setFavouriteColour(Colour favouriteColour) {
            this.favouriteColour = favouriteColour;            
        }

        public Float getSomeNumber() {
            return someNumber;
        }

        public void setSomeNumber(Float someNumber) {
           this.someNumber = someNumber;            
        }

        public List<TestBean> getFriends() {
            return friends;
        }

        public void setFriends(List<TestBean> friends) {
            this.friends = friends;            
        }
        
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (other == null || !(other instanceof TestBean)) {
                return false;
            }
            TestBean tb2 = (TestBean) other;
            return (ObjectUtils.nullSafeEquals(this.name, tb2.name) && this.age == tb2.age);
        }

        public int hashCode() {
            return this.age;
        }
        
        public int compareTo(Object other) {
            if (this.name != null && other instanceof TestBean) {
                return this.name.compareTo(((TestBean) other).getName());
            }
            else {
                return 1;
            }
        }

        public String toString() {
            return this.name;
        }
    }
    
    enum TestEnum {

        VALUE_1, VALUE_2;

        public String getEnumLabel() {
            return "Label: " + name();
        }

        public String getEnumValue() {
            return "Value: " + name();
        }

        public String toString() {
            return "TestEnum: " + name();
        }
    }
    
    protected static class TestBeanWithRealCountry extends TestBean {

        private Country realCountry = Country.COUNTRY_AT;

        public void setRealCountry(Country realCountry) {
            this.realCountry = realCountry;
        }

        public Country getRealCountry() {
            return realCountry;
        }
    }

}
