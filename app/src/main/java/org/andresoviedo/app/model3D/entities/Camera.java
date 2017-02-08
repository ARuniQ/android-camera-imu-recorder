package org.andresoviedo.app.model3D.entities;

// http://stackoverflow.com/questions/14607640/rotating-a-vector-in-3d-space

import android.opengl.Matrix;
import android.util.Log;

/*
 Class Name:

 CCamera.

 Created by:

 Allen Sherrod (Programming Ace of www.UltimateGameProgramming.com).

 Description:

 This class represents a camera in a 3D scene.
 */

public class Camera {

	public static final float UP = 0.5f; // Forward speed.
	public static final float DOWN = -0.5f; // Backward speed.
	public static final float LEFT = 0.5f; // Left speed.
	public static final float RIGHT = -0.5f; // Right speed.
	public static final float STRAFE_LEFT = -0.5f; // Left straft speed.
	public static final float STRAFE_RIGHT = 0.5f; // Right straft speed.

	public static final int AIM = 10;

	public float xPos, yPos; // Camera position.
	public float zPos;
	public float xView, yView, zView; // Look at position.
	public float xUp, yUp, zUp; // Up direction.
	public float radians, Xaxis, Yaxis, Zaxis;
	public float objRadians = 0, Xobj = 0, Yobj = 0, Zobj = 0;

	float xStrafe = 0, yStrafe = 0, zStrafe = 0; // Strafe direction.
	float currentRotationAngle; // Keeps us from going too far up or down.

	float[] buffer = new float[12 + 12 + 16 + 16];
	private boolean changed = false;

	public Camera() {
		// Initialize variables...
		this(0, 0, 3, 0, 0, -1, 0, 1, 0);

	}

	public Camera(float xPos, float yPos, float zPos, float xView, float yView, float zView, float xUp, float yUp,
			float zUp) {
		// Here we set the camera to the values sent in to us. This is mostly
		// used to set up a
		// default position.
		this.xPos = xPos;
		this.yPos = yPos;
		this.zPos = zPos;
		this.xView = xView;
		this.yView = yView;
		this.zView = zView;
		this.xUp = xUp;
		this.yUp = yUp;
		this.zUp = zUp;
	}

	private void normalize() {
		float xLook = 0, yLook = 0, zLook = 0;
		float xRight = 0, yRight = 0, zRight = 0;
		float xArriba = 0, yArriba = 0, zArriba = 0;
		float vlen;

		// Translating the camera requires a directional vector to rotate
		// First we need to get the direction at which we are looking.
		// The look direction is the view minus the position (where we are).
		// Get the Direction of the view.
		xLook = xView - xPos;
		yLook = yView - yPos;
		zLook = zView - zPos;
		vlen = Matrix.length(xLook, yLook, zLook);
		xLook /= vlen;
		yLook /= vlen;
		zLook /= vlen;

		// Next we get the axis which is a perpendicular vector of the view
		// direction and up values.
		// We use the cross product of that to get the axis then we normalize
		// it.
		xArriba = xUp - xPos;
		yArriba = yUp - yPos;
		zArriba = zUp - zPos;
		// Normalize the Right.
		vlen = Matrix.length(xArriba, yArriba, zArriba);
		xArriba /= vlen;
		yArriba /= vlen;
		zArriba /= vlen;

		// // Get the cross product of the direction and the up.
		// xRight = (yLook * zArriba) - (zLook * yArriba);
		// yRight = (zLook * xArriba) - (xLook * zArriba);
		// zRight = (xLook * yArriba) - (yLook * xArriba);
		// // Normalize the Right.
		// vlen = Matrix.length(xRight, yRight, zRight);
		// xRight /= vlen;
		// yRight /= vlen;
		// zRight /= vlen;

		xView = xLook + xPos;
		yView = yLook + yPos;
		zView = zLook + zPos;
		xUp = xArriba + xPos;
		yUp = yArriba + yPos;
		zUp = zArriba + zPos;
	}

	public void MoveCameraZ(float direction) {
		// Moving the camera requires a little more then adding 1 to the z or
		// subracting 1.
		// First we need to get the direction at which we are looking.
		float xLookDirection = 0, yLookDirection = 0, zLookDirection = 0;

		// The look direction is the view minus the position (where we are).
		xLookDirection = xView - xPos;
		yLookDirection = yView - yPos;
		zLookDirection = zView - zPos;

		// Normalize the direction.
		float dp = Matrix.length(xLookDirection, yLookDirection, zLookDirection);
		xLookDirection /= dp;
		yLookDirection /= dp;
		zLookDirection /= dp;

		// Call UpdateCamera to move our camera in the direction we want.
		UpdateCamera(xLookDirection, yLookDirection, zLookDirection, direction);
	}

	void UpdateCamera(float xDir, float yDir, float zDir, float dir) {
		// Move the camera on the X and Z axis. Notice I commented out the yPos
		// and yView
		// updates. This is because without them we can keep the camera on the
		// ground in
		// the simple camera tutorials without having to alter them afterwards.
		// xPos += xDir * dir;
		// yPos += yDir * dir;
		// zPos += zDir * dir;
		//
		// // Move the view along with the position
		// xView += xDir * dir;
		// yView += yDir * dir;
		// zView += zDir * dir;

		float[] matrix = new float[16];
		Matrix.setIdentityM(matrix, 0);
		Matrix.translateM(matrix, 0, -xPos, -yPos, -zPos);
		Matrix.translateM(matrix, 0, xDir * dir, yDir * dir, zDir * dir);
		Matrix.translateM(matrix, 0, xPos, yPos, zPos);

		float[] buffer = new float[12];
		Matrix.multiplyMV(buffer, 0, matrix, 0, getLocationVector(), 0);
		Matrix.multiplyMV(buffer, 4, matrix, 0, getLocationViewVector(), 0);
		Matrix.multiplyMV(buffer, 8, matrix, 0, getLocationUpVector(), 0);

		xPos = buffer[0] / buffer[3];
		yPos = buffer[1] / buffer[3];
		zPos = buffer[2] / buffer[3];
		xView = buffer[4] / buffer[7];
		yView = buffer[5] / buffer[7];
		zView = buffer[6] / buffer[7];
		xUp = buffer[8] / buffer[11];
		yUp = buffer[9] / buffer[11];
		zUp = buffer[10] / buffer[11];

		setChanged(true);
	}

	public void StrafeCam(float dX, float dY) {
		// Now if we were to call UpdateCamera() we will be moving the camera
		// foward or backwards.
		// We don't want that here. We want to strafe. To do so we have to get
		// the cross product
		// of our direction and Up direction view. The up was set in SetCamera
		// to be 1 positive
		// y. That is because anything positive on the y is considered up. After
		// we get the
		// cross product we can save it to the strafe variables so that can be
		// added to the
		// camera using UpdateCamera().

		float vlen;

		// Translating the camera requires a directional vector to rotate
		// First we need to get the direction at which we are looking.
		// The look direction is the view minus the position (where we are).
		// Get the Direction of the view.
		float xLook = 0, yLook = 0, zLook = 0;
		xLook = xView - xPos;
		yLook = yView - yPos;
		zLook = zView - zPos;
		vlen = Matrix.length(xLook, yLook, zLook);
		xLook /= vlen;
		yLook /= vlen;
		zLook /= vlen;

		// Next we get the axis which is a perpendicular vector of the view
		// direction and up values.
		// We use the cross product of that to get the axis then we normalize
		// it.
		float xArriba = 0, yArriba = 0, zArriba = 0;
		xArriba = xUp - xPos;
		yArriba = yUp - yPos;
		zArriba = zUp - zPos;
		// Normalize the Right.
		vlen = Matrix.length(xArriba, yArriba, zArriba);
		xArriba /= vlen;
		yArriba /= vlen;
		zArriba /= vlen;

		// Get the cross product of the direction and the up.
		float xRight = 0, yRight = 0, zRight = 0;
		xRight = (yLook * zArriba) - (zLook * yArriba);
		yRight = (zLook * xArriba) - (xLook * zArriba);
		zRight = (xLook * yArriba) - (yLook * xArriba);
		// Normalize the Right.
		vlen = Matrix.length(xRight, yRight, zRight);
		xRight /= vlen;
		yRight /= vlen;
		zRight /= vlen;

		// Calculate sky / up
		float xSky = 0, ySky = 0, zSky = 0;

		// Get the cross product of the direction and the up.
		xSky = (yRight * zLook) - (zRight * yLook);
		ySky = (zRight * xLook) - (xRight * zLook);
		zSky = (xRight * yLook) - (yRight * xLook);
		// Normalize the sky / up.
		vlen = Matrix.length(xSky, ySky, zSky);
		xSky /= vlen;
		ySky /= vlen;
		zSky /= vlen;

		// UpdateCamera(xRight, yRight, zRight, dX);
		UpdateCamera(xSky, ySky, zSky, dX);
	}

	public void RotateCamera(float AngleDir, float xSpeed, float ySpeed, float zSpeed) {
		float xNewLookDirection = 0, yNewLookDirection = 0, zNewLookDirection = 0;
		float xLookDirection = 0, yLookDirection = 0, zLookDirection = 0;
		float CosineAngle = 0, SineAngle = 0;

		// System.out.println("AngleDir[" + AngleDir + "]");

		// First we will need to calculate the cos and sine of our angle. I
		// creaetd two macros to
		// do this in the CCamera.h header file called GET_COS and GET_SINE. To
		// use the macros
		// we just send in the variable we ant to store the results and the
		// angle we need to
		// calculate.
		CosineAngle = (float) Math.cos(AngleDir);
		SineAngle = (float) Math.sin(AngleDir);

		// Next get the look direction (where we are looking) just like in the
		// move camera function.
		xLookDirection = xView - xPos;
		yLookDirection = yView - yPos;
		zLookDirection = zView - zPos;

		// Normalize the direction.
		float dp = 1 / (float) Math.sqrt(
				xLookDirection * xLookDirection + yLookDirection * yLookDirection + zLookDirection * zLookDirection);
		xLookDirection *= dp;
		yLookDirection *= dp;
		zLookDirection *= dp;

		// Calculate the new X position.
		xNewLookDirection = (CosineAngle + (1 - CosineAngle) * xSpeed) * xLookDirection;
		xNewLookDirection += ((1 - CosineAngle) * xSpeed * ySpeed - zSpeed * SineAngle) * yLookDirection;
		xNewLookDirection += ((1 - CosineAngle) * xSpeed * zSpeed + ySpeed * SineAngle) * zLookDirection;

		// Calculate the new Y position.
		yNewLookDirection = ((1 - CosineAngle) * xSpeed * ySpeed + zSpeed * SineAngle) * xLookDirection;
		yNewLookDirection += (CosineAngle + (1 - CosineAngle) * ySpeed) * yLookDirection;
		yNewLookDirection += ((1 - CosineAngle) * ySpeed * zSpeed - xSpeed * SineAngle) * zLookDirection;

		// Calculate the new Z position.
		zNewLookDirection = ((1 - CosineAngle) * xSpeed * zSpeed - ySpeed * SineAngle) * xLookDirection;
		zNewLookDirection += ((1 - CosineAngle) * ySpeed * zSpeed + xSpeed * SineAngle) * yLookDirection;
		zNewLookDirection += (CosineAngle + (1 - CosineAngle) * zSpeed) * zLookDirection;

		// Last we add the new rotations to the old view to correctly rotate the
		// camera.
		xView = xPos + xNewLookDirection;
		yView = yPos + yNewLookDirection;
		zView = zPos + zNewLookDirection;
	}

	public void Rotate(float incX, float incY) {
		RotateByMouse(AIM + incX, AIM + incY, AIM, AIM);
	}

	void RotateByMouse(float mousePosX, float mousePosY, float midX, float midY) {
		float yDirection = 0.0f; // Direction angle.
		float yRotation = 0.0f; // Rotation angle.

		// If the mouseX and mouseY are at the middle of the screen then we
		// can't rotate the view.
		if ((mousePosX == midX) && (mousePosY == midY))
			return;

		// Next we get the direction of each axis. We divide by 1000 to get a
		// smaller value back.
		yDirection = (float) ((midX - mousePosX)) / 1.0f;
		yRotation = (float) ((midY - mousePosY)) / 1.0f;

		// We use curentRotX to help use keep the camera from rotating too far
		// in either direction.
		currentRotationAngle -= yRotation;

		// Stop the camera from going to high...
		if (currentRotationAngle > 1.5f) {
			currentRotationAngle = 1.5f;
			return;
		}

		// Stop the camera from going to low...
		if (currentRotationAngle < -1.5f) {
			currentRotationAngle = -1.5f;
			return;
		}

		// Next we get the axis which is a perpendicular vector of the view
		// direction and up values.
		// We use the cross product of that to get the axis then we normalize
		// it.
		float xAxis = 0, yAxis = 0, zAxis = 0;
		float xDir = 0, yDir = 0, zDir = 0;

		// Get the Direction of the view.
		xDir = xView - xPos;
		yDir = yView - yPos;
		zDir = zView - zPos;

		// Get the cross product of the direction and the up.
		xAxis = (yDir * zUp) - (zDir * yUp);
		yAxis = (zDir * xUp) - (xDir * zUp);
		zAxis = (xDir * yUp) - (yDir * xUp);

		// Normalize it.
		float len = 1 / (float) Math.sqrt(xAxis * xAxis + yAxis * yAxis + zAxis * zAxis);
		xAxis *= len;
		yAxis *= len;
		zAxis *= len;

		// Rotate the camera.
		RotateCamera(yRotation, xAxis, yAxis, zAxis);
		RotateCamera(yDirection, 0, 1, 0);
	}

	public void translateCamera(float dX, float dY) {
		float xLook = 0, yLook = 0, zLook = 0;
		float xArriba = 0, yArriba = 0, zArriba = 0;
		float vlen;

		// Translating the camera requires a directional vector to rotate
		// First we need to get the direction at which we are looking.
		// The look direction is the view minus the position (where we are).
		// Get the Direction of the view.
		xLook = xView - xPos;
		yLook = yView - yPos;
		zLook = zView - zPos;
		vlen = Matrix.length(xLook, yLook, zLook);
		xLook /= vlen;
		yLook /= vlen;
		zLook /= vlen;

		// Next we get the axis which is a perpendicular vector of the view
		// direction and up values.
		// We use the cross product of that to get the axis then we normalize
		// it.
		float xRight = 0, yRight = 0, zRight = 0;
		xArriba = xUp - xPos;
		yArriba = yUp - yPos;
		zArriba = zUp - zPos;
		// Normalize the Right.
		vlen = Matrix.length(xArriba, yArriba, zArriba);
		xArriba /= vlen;
		yArriba /= vlen;
		zArriba /= vlen;

		// Get the cross product of the direction and the up.
		// A x B = (a1, a2, a3) x (b1, b2, b3) = (a2 * b3 - b2 * a3 , - a1 * b3 + b1 * a3 , a1 * b2 - b1 * a2)
		xRight = (yLook * zArriba) - (zLook * yArriba);
		yRight = (zLook * xArriba) - (xLook * zArriba);
		zRight = (xLook * yArriba) - (yLook * xArriba);
		// Normalize the Right.
		vlen = Matrix.length(xRight, yRight, zRight);
		xRight /= vlen;
		yRight /= vlen;
		zRight /= vlen;

		// Once we have the look & right, we can recalculate where is the final up vector
		xArriba = (yRight * zLook) - (zRight * yLook);
		yArriba = (zRight * xLook) - (xRight * zLook);
		zArriba = (xRight * yLook) - (yRight * xLook);
		// Normalize the Right.
		vlen = Matrix.length(xArriba, yArriba, zArriba);
		xArriba /= vlen;
		yArriba /= vlen;
		zArriba /= vlen;

		float[] coordinates = new float[] { xPos, yPos, zPos, 1, xView, yView, zView, 1, xUp, yUp, zUp, 1 };

		createRotationMatrixAroundVector(buffer, 24, dX, xArriba, yArriba, zArriba);
		multiplyMMV(buffer, 0, buffer, 24, coordinates, 0);
		multiplyMMV(buffer, 12, buffer, 40, coordinates, 0);

		xPos = buffer[0] / buffer[3];
		yPos = buffer[1] / buffer[3];
		zPos = buffer[2] / buffer[3];
		xView = buffer[4 + 0] / buffer[4 + 3];
		yView = buffer[4 + 1] / buffer[4 + 3];
		zView = buffer[4 + 2] / buffer[4 + 3];
		xUp = buffer[8 + 0] / buffer[8 + 3];
		yUp = buffer[8 + 1] / buffer[8 + 3];
		zUp = buffer[8 + 2] / buffer[8 + 3];

		createRotationMatrixAroundVector(buffer, 40, dY, xRight, yRight, zRight);
		coordinates = new float[] { xPos, yPos, zPos, 1, xView, yView, zView, 1, xUp, yUp, zUp, 1 };
		multiplyMMV(buffer, 0, buffer, 40, coordinates, 0);

		xPos = buffer[0] / buffer[3];
		yPos = buffer[1] / buffer[3];
		zPos = buffer[2] / buffer[3];
		xView = buffer[4 + 0] / buffer[4 + 3];
		yView = buffer[4 + 1] / buffer[4 + 3];
		zView = buffer[4 + 2] / buffer[4 + 3];
		xUp = buffer[8 + 0] / buffer[8 + 3];
		yUp = buffer[8 + 1] / buffer[8 + 3];
		zUp = buffer[8 + 2] / buffer[8 + 3];

		setChanged(true);

	}

	public void rotateCamera(float q0, float q1, float q2, float q3) {
		// here the q0 - q3 are quaternions

		float radians = (float) (Math.acos(q0) * 2);

		this.radians = radians;
		this.Xaxis = q1;
		this.Yaxis = q2;
		this.Zaxis = q3;

		setChanged(true);

	}

	public void setObjectRotation() {
		this.objRadians = this.radians;
		this.Xobj = this.Xaxis;
		this.Yobj = this.Yaxis;
		this.Zobj = this.Zaxis;
	}

	public String locationToString() {
		return xPos + "," + yPos + "," + zPos;
	}

	public String ToStringVector() {
		return xPos + "," + yPos + "," + zPos + " ; " + xView + "," + yView + "," + zView + " ; " + xUp + "," + yUp
				+ "," + zUp;
	}

	public float[] getVectors() {
		// @formatter:off
		return new float[] { 
				xPos, yPos, zPos, 1f, 
				xView, yView, yView, 1f,
				xUp, yUp, zUp, 1f };
		// @formatter:on
	}

	public static void createRotationMatrixAroundVector(float[] matrix, int offset,
														float angle, float x, float y, float z) {
		float cos = (float) Math.cos(angle);
		float sin = (float) Math.sin(angle);
		float cos_1 = 1 - cos;

		// @formatter:off
		matrix[offset+0 ]=cos_1*x*x + cos     ;    	matrix[offset+1 ]=cos_1*x*y - z*sin   ;   matrix[offset+2 ]=cos_1*z*x + y*sin   ;   matrix[offset+3]=0   ;
		matrix[offset+4 ]=cos_1*x*y + z*sin   ;  	matrix[offset+5 ]=cos_1*y*y + cos     ;   matrix[offset+6 ]=cos_1*y*z - x*sin   ;   matrix[offset+7]=0   ;
		matrix[offset+8 ]=cos_1*z*x - y*sin   ;  	matrix[offset+9 ]=cos_1*y*z + x*sin   ;   matrix[offset+10]=cos_1*z*z + cos    ;   matrix[offset+11]=0  ;
		matrix[offset+12]=0           		 ;      matrix[offset+13]=0          		  ;   matrix[offset+14]=0          		  ;   matrix[offset+15]=1  ;
		
		// @formatter:on
	}

	public static void createTranslationMatrix(float[] matrix, int offset, float x, float y, float z) {
		matrix[offset+0 ]=1;    matrix[offset+1 ]=0;   matrix[offset+2 ]=0;   matrix[offset+3]=0   ;
		matrix[offset+4 ]=0;  	matrix[offset+5 ]=1;   matrix[offset+6 ]=0;   matrix[offset+7]=0  ;
		matrix[offset+8 ]=0;  	matrix[offset+9 ]=0;   matrix[offset+10]=1;   matrix[offset+11]=0  ;
		matrix[offset+12]=-x;    matrix[offset+13]=-y;   matrix[offset+14]=-z;   matrix[offset+15]=1  ;
	}

	public static void multiplyMMV(float[] result, int retOffset, float[] matrix, int matOffet, float[] vector4Matrix,
			int vecOffset) {
		for (int i = 0; i < 3; i++) {
			Matrix.multiplyMV(result, retOffset + (i * 4), matrix, matOffet, vector4Matrix, vecOffset + (i * 4));
		}
	}

	public float[] getLocationVector() {
		return new float[] { xPos, yPos, zPos, 1f };
	}

	public float[] getLocationViewVector() {
		return new float[] { xView, yView, zView, 1f };
	}

	public float[] getLocationUpVector() {
		return new float[] { xUp, yUp, zUp, 1f };
	}

	public String intLocationToString() {
		return (float) (xPos) + "," + (float) yPos + "," + (float) zPos;
	}

	public boolean hasChanged() {
		return changed;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	@Override
	public String toString() {
		return "Camera [xPos=" + xPos + ", yPos=" + yPos + ", zPos=" + zPos + ", xView=" + xView + ", yView=" + yView
				+ ", zView=" + zView + ", xUp=" + xUp + ", yUp=" + yUp + ", zUp=" + zUp + "]";
	}

	public void Rotate(float rotViewerZ) {
		if (Float.isNaN(rotViewerZ)) {
			Log.w("Rot", "NaN");
			return;
		}
		float xLook = xView - xPos;
		float yLook = yView - yPos;
		float zLook = zView - zPos;
		float vlen = Matrix.length(xLook, yLook, zLook);
		xLook /= vlen;
		yLook /= vlen;
		zLook /= vlen;

		createRotationMatrixAroundVector(buffer, 24, rotViewerZ, xLook, yLook, zLook);
		float[] coordinates = new float[] { xPos, yPos, zPos, 1, xView, yView, zView, 1, xUp, yUp, zUp, 1 };
		multiplyMMV(buffer, 0, buffer, 24, coordinates, 0);

		xPos = buffer[0];
		yPos = buffer[1];
		zPos = buffer[2];
		xView = buffer[4 + 0];
		yView = buffer[4 + 1];
		zView = buffer[4 + 2];
		xUp = buffer[8 + 0];
		yUp = buffer[8 + 1];
		zUp = buffer[8 + 2];

		setChanged(true);
	}

}