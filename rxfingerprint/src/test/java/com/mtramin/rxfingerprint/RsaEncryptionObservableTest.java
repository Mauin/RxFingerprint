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

import com.mtramin.rxfingerprint.data.FingerprintEncryptionResult;
import com.mtramin.rxfingerprint.data.FingerprintUnavailableException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.crypto.Cipher;

import io.reactivex.Observable;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Cipher.class)
public class RsaEncryptionObservableTest {

	private static final String INPUT = "TEST";

	@Mock FingerprintApiWrapper fingerprintApiWrapper;
	@Mock RsaCipherProvider cipherProvider;
	Cipher cipher;

	private Observable<FingerprintEncryptionResult> observable;

	@Before
	public void setUp() throws Exception {
		mockStatic(Cipher.class);
		cipher = mock(Cipher.class);
		RxFingerprint.disableLogging();

		observable = Observable.create(new RsaEncryptionObservable(fingerprintApiWrapper, cipherProvider, INPUT.toCharArray(), new TestEncodingProvider()));
	}

	@Test
	public void blocksEncryptionWhenFingerprintUnavailable() throws Exception {
		when(fingerprintApiWrapper.isUnavailable()).thenReturn(true);

		observable.test()
				.assertNoValues()
				.assertError(FingerprintUnavailableException.class);
	}

	@Test
	public void chiperCreationThrows() throws Exception {
		when(fingerprintApiWrapper.isUnavailable()).thenReturn(false);
		when(cipherProvider.getCipherForEncryption()).thenThrow(SecurityException.class);

		observable.test()
				.assertNoValues()
				.assertError(SecurityException.class);
	}

	@Test
	public void encrypt() throws Exception {
		when(fingerprintApiWrapper.isUnavailable()).thenReturn(false);
		when(cipherProvider.getCipherForEncryption()).thenReturn(cipher);
		when(cipher.doFinal(ConversionUtils.toBytes(INPUT.toCharArray()))).thenReturn(ConversionUtils.toBytes(INPUT.toCharArray()));

		FingerprintEncryptionResult fingerprintEncryptionResult = observable.test()
				.assertValueCount(1)
				.assertNoErrors()
				.assertComplete()
				.values().get(0);

		assertEquals(INPUT, fingerprintEncryptionResult.getEncrypted());
	}
}
