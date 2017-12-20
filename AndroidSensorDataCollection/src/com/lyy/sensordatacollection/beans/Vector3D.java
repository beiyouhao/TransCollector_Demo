package com.lyy.sensordatacollection.beans;

public class Vector3D {
	private float x;
	private float y;
	private float z;
	
	public Vector3D(float x, float y, float z) {
		super();
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public void setValues(float [] values){
		this.x = values[0];
		this.y = values[1];
		this.z = values[2];
	}
	
	public float getX() {
		return x;
	}
	public void setX(float x) {
		this.x = x;
	}
	public float getY() {
		return y;
	}
	public void setY(float y) {
		this.y = y;
	}
	public float getZ() {
		return z;
	}
	public void setZ(float z) {
		this.z = z;
	}
	
	@Override
	public boolean equals(Object o) {
		// TODO Auto-generated method stub
		Vector3D vector3d = (Vector3D)o;
		return this.x==vector3d.x&&this.y==vector3d.y&&this.z==vector3d.z;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return x+" "+y+" "+z+" ";
	}
}
