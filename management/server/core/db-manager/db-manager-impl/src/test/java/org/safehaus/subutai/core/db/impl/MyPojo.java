/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.db.impl;


/**
 * Test pojo
 */
public class MyPojo {

	private final String content;


	public MyPojo(String test) {
		this.content = test;
	}


	@Override
	public int hashCode() {
		return 5;
	}


	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final MyPojo other = (MyPojo) obj;
		return !((this.content == null) ? (other.getContent() != null) :
				!this.content.equals(other.getContent()));
	}


	public String getContent() {
		return content;
	}


	@Override
	public String toString() {
		return "MyPojo{" + "content=" + content + '}';
	}
}
