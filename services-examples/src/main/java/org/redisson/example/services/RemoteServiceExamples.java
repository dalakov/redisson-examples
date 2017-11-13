/**
 * Copyright 2016 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.example.services;

import org.redisson.Redisson;
import org.redisson.api.RFuture;
import org.redisson.api.RRemoteService;
import org.redisson.api.RedissonClient;
import org.redisson.api.annotation.RRemoteAsync;

public class RemoteServiceExamples {

    public interface RemoteInterface { Integer myMethod(Integer z); }

    @RRemoteAsync(RemoteInterface.class)
    public interface RemoteInterfaceAsync { RFuture<Integer> myMethod(Integer z); }


    public static class RemoteImpl implements RemoteInterface {

        public RemoteImpl() {
        }

        @Override
        public Integer myMethod(Integer z) {
/*
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
*/
            return z*10;
        }

    }

    public static void main(String[] args) throws InterruptedException {
        // connects to 127.0.0.1:6379 by default
        RedissonClient server = Redisson.create();
        RedissonClient client = Redisson.create();
        try {
            RRemoteService serverRemoteService1 = server.getRemoteService();

            RemoteImpl someServiceImpl = new RemoteImpl();
            serverRemoteService1.register(RemoteInterface.class, someServiceImpl,2);
            Thread.sleep(100);

            RRemoteService clientRemoteService = client.getRemoteService();
            //RemoteInterface service = clientRemoteService .get(RemoteInterface.class);
            RemoteInterfaceAsync asyncService = clientRemoteService.get(RemoteInterfaceAsync.class);

            asyncService.myMethod(10);
            long start = System.currentTimeMillis();

            for (int i=0;i<10000;i++ ) asyncService.myMethod(10);

            long timeSpent = System.currentTimeMillis() - start;
            System.out.println(" timeSpent " + timeSpent + " ms");

            Thread.sleep(10000);

        } finally {
            client.shutdown();
            server.shutdown();
        }

    }

}
