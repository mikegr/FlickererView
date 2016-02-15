/*
        Copyright 2016 BeeOne GmbH

        Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
*/
package at.beeone.flickerer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FlickerThread {


    private final ArrayList<boolean[]> displayCode;
    private final int frequency;
    private final Callback callback;
    public boolean interrupted = false;
    private Runnable runnable;

    public FlickerThread(String code, int frequency, Callback callback) {
        this.callback = callback;
        this.frequency = frequency;
        Map<String, boolean[]> map = new HashMap<>();
        for(byte i = i = 0 ; i < 16; i++) {
            map.put(Integer.toHexString(i).toUpperCase(), new boolean[] {false, (i & 0b0001) != 0, (i & 0b0010) != 0, (i & 0b0100) != 0,  (i & 0b1000) != 0 });
        }
        this.displayCode = new ArrayList<>();
        for (int i = 0; i < code.length(); i += 2) {
            displayCode.add(map.get(Character.toString(code.charAt(i + 1))));
            displayCode.add(map.get(Character.toString(code.charAt(i))));
        }
    }

    public void start() {
        interrupted = false;
        runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        for (int i = 0; i < displayCode.size(); i++) {
                            synchronized (this) {
                                if (interrupted) {
                                    callback.stopped();
                                    return;
                                }
                            }
                            boolean[] code = displayCode.get(i);
                            code[0] = true;
                            callback.display(code);
                            Thread.sleep(1000 / frequency);
                            code[0] = false;
                            callback.display(code);
                            Thread.sleep(1000 / frequency);
                        }
                    }
                } catch (InterruptedException e) {
                }
            }
        };
        new Thread(runnable, "FlickerThread").start();
    }

    public void stop() {
        synchronized (runnable) {
            interrupted = true;
        }
    }

    public boolean isStopped() {
        synchronized (runnable) {
            return interrupted;
        }
    }
    public interface Callback {
        void display(boolean[] state);
        void stopped();
    }
}
