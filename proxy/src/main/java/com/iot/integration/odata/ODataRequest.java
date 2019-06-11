package com.iot.integration.odata;

import java.net.URLEncoder;

import org.springframework.util.StringUtils;

public class ODataRequest {

	private String url;
	private String entitySet;
	private String format;
	private String select;
	private String filter;
	private String expand;
	private String objectKey;
	private String top;
	private String token;
	private String authorizationHeaderName = "Authorization";
	
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getEntitySet() {
		return entitySet;
	}
	public void setEntitySet(String entitySet) {
		this.entitySet = entitySet;
	}
	
	public String getAuthorizationHeaderName() {
		return authorizationHeaderName;
	}
	public void setAuthorizationHeaderName(String authorizationHeaderName) {
		this.authorizationHeaderName = authorizationHeaderName;
	}
	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}
	public String getSelect() {
		return select;
	}
	public void setSelect(String select) {
		try {
			if (select != null)
				this.select = URLEncoder.encode(select, "UTF-8");
			else 
				this.select = null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public String getFilter() {
		return filter;
	}
	public void setFilter(String filter) {
		try {
			if (filter != null)
				this.filter = URLEncoder.encode(filter, "UTF-8");
			else
				this.filter = null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public String getExpand() {
		return expand;
	}
	public void setExpand(String expand) {
		try {
			if(expand != null)
				this.expand = URLEncoder.encode(expand, "UTF-8");
			else
				this.expand = null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	public String getTop() {
		return top;
	}
	
	public void setTop(String top) {
		try {
			if(top != null)
				this.top = URLEncoder.encode(top, "UTF-8");
			else
				this.top = null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getObjectKey() {
		return objectKey;
	}
	public void setObjectKey(String objectKey) {
		try {
			if(objectKey != null)
				this.objectKey = URLEncoder.encode(objectKey, "UTF-8");
			else
				this.objectKey = null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	public String getRequestString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append(url);
		buffer.append("/");
		buffer.append(entitySet);
		if ( objectKey != null && !StringUtils.trimAllWhitespace(objectKey).equalsIgnoreCase("")) {
			buffer.append("('");
			buffer.append(objectKey);
			buffer.append("')");
		}
		buffer.append("?");
		
		boolean moreParams = false;
		if ( format != null ) {
			buffer.append("$format=");
			buffer.append(format);
			moreParams = true;
		}
		
		if ( select != null ) {
			if ( moreParams ) {
				buffer.append("&");
			}
			buffer.append("$select=");
			buffer.append(select);
			moreParams = true;
		}
		
		if ( filter != null ) {
			if ( moreParams ) {
				buffer.append("&");
			}
			buffer.append("$filter=");
			buffer.append(filter);
			moreParams = true;
		}
		
		if ( expand != null ) {
			if ( moreParams ) {
				buffer.append("&");
			}
			buffer.append("$expand=");
			buffer.append(expand);
			moreParams = true;
		}
		
		if ( top != null ) {
			if ( moreParams ) {
				buffer.append("&");
			}
			buffer.append("$top=");
			buffer.append(top);
			moreParams = true;
		}
		
		return buffer.toString();
	}
}
