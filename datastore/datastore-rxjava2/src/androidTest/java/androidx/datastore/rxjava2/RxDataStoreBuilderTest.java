/*
 * Copyright 2020 The Android Open Source Project
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
package androidx.datastore.rxjava2;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;

import androidx.datastore.core.DataStore;
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler;
import androidx.test.core.app.ApplicationProvider;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

public class RxDataStoreBuilderTest {
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private static Single<Byte> incrementByte(Byte byteIn) {
        return Single.just(++byteIn);
    }

    @Test
    public void testConstructWithProduceFile() throws Exception {
        File file = tempFolder.newFile();
        DataStore<Byte> dataStore =
                new RxDataStoreBuilder<Byte>(() -> file, new TestingSerializer())
                        .build();
        Single<Byte> incrementByte = RxDataStore.updateDataAsync(dataStore,
                RxDataStoreBuilderTest::incrementByte);
        assertThat(incrementByte.blockingGet()).isEqualTo(1);
        // Construct it again and confirm that the data is still there:
        dataStore =
                new RxDataStoreBuilder<Byte>(() -> file, new TestingSerializer())
                        .build();
        assertThat(RxDataStore.data(dataStore).blockingFirst()).isEqualTo(1);
    }

    @Test
    public void testConstructWithContextAndName() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        String name = "my_data_store";
        DataStore<Byte> dataStore =
                new RxDataStoreBuilder<Byte>(context, name, new TestingSerializer())
                        .build();
        Single<Byte> set1 = RxDataStore.updateDataAsync(dataStore, input -> Single.just((byte) 1));
        assertThat(set1.blockingGet()).isEqualTo(1);
        // Construct it again and confirm that the data is still there:
        dataStore =
                new RxDataStoreBuilder<Byte>(context, name, new TestingSerializer())
                        .build();
        assertThat(RxDataStore.data(dataStore).blockingFirst()).isEqualTo(1);
        // Construct it again with the expected file path and confirm that the data is there:
        dataStore =
                new RxDataStoreBuilder<Byte>(() -> new File(context.getFilesDir().getPath()
                        + "/datastore/" + name), new TestingSerializer()
                )
                        .build();
        assertThat(RxDataStore.data(dataStore).blockingFirst()).isEqualTo(1);
    }

    @Test
    public void testSpecifiedSchedulerIsUser() throws Exception {
        Scheduler singleThreadedScheduler =
                Schedulers.from(Executors.newSingleThreadExecutor(new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "TestingThread");
                    }
                }));


        DataStore<Byte> dataStore = new RxDataStoreBuilder<Byte>(() -> tempFolder.newFile(),
                new TestingSerializer())
                .setIoScheduler(singleThreadedScheduler)
                .build();
        Single<Byte> update = RxDataStore.updateDataAsync(dataStore, input -> {
            Thread currentThread = Thread.currentThread();
            assertThat(currentThread.getName()).isEqualTo("TestingThread");
            return Single.just(input);
        });
        assertThat(update.blockingGet()).isEqualTo((byte) 0);
        Single<Byte> subsequentUpdate = RxDataStore.updateDataAsync(dataStore, input -> {
            Thread currentThread = Thread.currentThread();
            assertThat(currentThread.getName()).isEqualTo("TestingThread");
            return Single.just(input);
        });
        assertThat(subsequentUpdate.blockingGet()).isEqualTo((byte) 0);
    }

    @Test
    public void testCorruptionHandlerIsUser() {
        TestingSerializer testingSerializer = new TestingSerializer();
        testingSerializer.setFailReadWithCorruptionException(true);
        ReplaceFileCorruptionHandler<Byte> replaceFileCorruptionHandler =
                new ReplaceFileCorruptionHandler<Byte>(exception -> (byte) 99);


        DataStore<Byte> dataStore = new RxDataStoreBuilder<Byte>(
                () -> tempFolder.newFile(),
                testingSerializer)
                .setCorruptionHandler(replaceFileCorruptionHandler)
                .build();
        assertThat(RxDataStore.data(dataStore).blockingFirst()).isEqualTo(99);
    }
}
