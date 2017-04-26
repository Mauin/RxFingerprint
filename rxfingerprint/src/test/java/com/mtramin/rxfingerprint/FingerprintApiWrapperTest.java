/*
 * Copyright 2017 Marvin Ramin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * <http://www.apache.org/licenses/LICENSE-2.0>
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mtramin.rxfingerprint;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static android.Manifest.permission.USE_FINGERPRINT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@SuppressLint("MissingPermission")
@RunWith(MockitoJUnitRunner.class)
public class FingerprintApiWrapperTest {

	@Mock Context context;
	@Mock FingerprintManager fingerprintManager;

	@Before
	public void setUp() throws Exception {
		RxFingerprint.disableLogging();
	}

	@Test
	public void oldSdkReportsUnavailable() throws Exception {
		TestHelper.setSdkLevel(21);
		FingerprintApiWrapper fingerprintApiWrapper = new FingerprintApiWrapper(context);
		assertTrue(fingerprintApiWrapper.isUnavailable());
		assertFalse(fingerprintApiWrapper.isAvailable());

		assertFalse(fingerprintApiWrapper.hasEnrolledFingerprints());
		assertFalse(fingerprintApiWrapper.isHardwareDetected());
	}

	@Test
	public void missingPermissionFailsFingerprintManagerChecks() throws Exception {
		TestHelper.setSdkLevel(23);
		when(context.checkSelfPermission(USE_FINGERPRINT)).thenReturn(PackageManager.PERMISSION_DENIED);

		FingerprintApiWrapper fingerprintApiWrapper = new FingerprintApiWrapper(context);
		assertTrue(fingerprintApiWrapper.isUnavailable());
		assertFalse(fingerprintApiWrapper.isAvailable());
		assertFalse(fingerprintApiWrapper.isHardwareDetected());
		assertFalse(fingerprintApiWrapper.hasEnrolledFingerprints());
	}

	@Test
	public void fingerprintApiFailure() throws Exception {
		TestHelper.setSdkLevel(23);
		when(context.checkSelfPermission(USE_FINGERPRINT)).thenReturn(PackageManager.PERMISSION_GRANTED);
		when(context.getSystemService(Context.FINGERPRINT_SERVICE)).thenThrow(NoClassDefFoundError.class);

		FingerprintApiWrapper fingerprintApiWrapper = new FingerprintApiWrapper(context);
		assertTrue(fingerprintApiWrapper.isUnavailable());
		assertFalse(fingerprintApiWrapper.isAvailable());
		assertFalse(fingerprintApiWrapper.isHardwareDetected());
		assertFalse(fingerprintApiWrapper.hasEnrolledFingerprints());
	}

	@Test
	public void noHardwareDetected() throws Exception {
		TestHelper.setSdkLevel(23);
		when(context.checkSelfPermission(USE_FINGERPRINT)).thenReturn(PackageManager.PERMISSION_GRANTED);
		when(context.getSystemService(Context.FINGERPRINT_SERVICE)).thenReturn(fingerprintManager);
		when(fingerprintManager.isHardwareDetected()).thenReturn(false);

		FingerprintApiWrapper fingerprintApiWrapper = new FingerprintApiWrapper(context);
		assertTrue(fingerprintApiWrapper.isUnavailable());
		assertFalse(fingerprintApiWrapper.isAvailable());
		assertFalse(fingerprintApiWrapper.isHardwareDetected());
	}

	@Test
	public void hardwareDetected() throws Exception {
		TestHelper.setSdkLevel(23);
		when(context.checkSelfPermission(USE_FINGERPRINT)).thenReturn(PackageManager.PERMISSION_GRANTED);
		when(context.getSystemService(Context.FINGERPRINT_SERVICE)).thenReturn(fingerprintManager);
		when(fingerprintManager.isHardwareDetected()).thenReturn(true);

		FingerprintApiWrapper fingerprintApiWrapper = new FingerprintApiWrapper(context);
		assertTrue(fingerprintApiWrapper.isHardwareDetected());
	}

	@Test
	public void noFingerprintsEnrolled() throws Exception {
		TestHelper.setSdkLevel(23);
		when(context.checkSelfPermission(USE_FINGERPRINT)).thenReturn(PackageManager.PERMISSION_GRANTED);
		when(context.getSystemService(Context.FINGERPRINT_SERVICE)).thenReturn(fingerprintManager);
		when(fingerprintManager.hasEnrolledFingerprints()).thenReturn(false);

		FingerprintApiWrapper fingerprintApiWrapper = new FingerprintApiWrapper(context);
		assertTrue(fingerprintApiWrapper.isUnavailable());
		assertFalse(fingerprintApiWrapper.isAvailable());
		assertFalse(fingerprintApiWrapper.hasEnrolledFingerprints());
	}

	@Test
	public void fingerprintsEnrolled() throws Exception {
		TestHelper.setSdkLevel(23);
		when(context.checkSelfPermission(USE_FINGERPRINT)).thenReturn(PackageManager.PERMISSION_GRANTED);
		when(context.getSystemService(Context.FINGERPRINT_SERVICE)).thenReturn(fingerprintManager);
		when(fingerprintManager.hasEnrolledFingerprints()).thenReturn(true);

		FingerprintApiWrapper fingerprintApiWrapper = new FingerprintApiWrapper(context);
		assertTrue(fingerprintApiWrapper.hasEnrolledFingerprints());
	}

	@Test
	public void isAvailable() throws Exception {
		TestHelper.setSdkLevel(23);
		when(context.checkSelfPermission(USE_FINGERPRINT)).thenReturn(PackageManager.PERMISSION_GRANTED);
		when(context.getSystemService(Context.FINGERPRINT_SERVICE)).thenReturn(fingerprintManager);
		when(fingerprintManager.hasEnrolledFingerprints()).thenReturn(true);
		when(fingerprintManager.isHardwareDetected()).thenReturn(true);

		FingerprintApiWrapper fingerprintApiWrapper = new FingerprintApiWrapper(context);
		assertTrue(fingerprintApiWrapper.isAvailable());
		assertFalse(fingerprintApiWrapper.isUnavailable());
	}

	@Test
	public void getFingerprintManager() throws Exception {
		TestHelper.setSdkLevel(23);
		when(context.checkSelfPermission(USE_FINGERPRINT)).thenReturn(PackageManager.PERMISSION_GRANTED);
		when(context.getSystemService(Context.FINGERPRINT_SERVICE)).thenReturn(fingerprintManager);
		when(fingerprintManager.hasEnrolledFingerprints()).thenReturn(true);
		when(fingerprintManager.isHardwareDetected()).thenReturn(true);

		FingerprintApiWrapper fingerprintApiWrapper = new FingerprintApiWrapper(context);
		assertEquals(fingerprintManager, fingerprintApiWrapper.getFingerprintManager());
	}

	@Test(expected =  IllegalStateException.class)
	public void getFingerprintManagerThrowsWhenUnavailable() throws Exception {
		TestHelper.setSdkLevel(21);

		FingerprintApiWrapper fingerprintApiWrapper = new FingerprintApiWrapper(context);
		fingerprintApiWrapper.getFingerprintManager();
	}
}
