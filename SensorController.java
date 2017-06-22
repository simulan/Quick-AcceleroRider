package be.nmct.bluetooth;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * TODO : add default gravity values => multiple devices
 */
public class SensorController implements SensorEventListener {
    private SensorManager _manager;
    private Sensor _acceleroMeter;
    private Acceleration _acceleration;
    private Rotation _rotation;
    private BluetoothController _bc;
    private Ride ride;

    public void setRide(Ride ride) {
        this.ride = ride;
    }

    public Ride getRide() {
        return ride;
    }

    public enum Ride {
        AUTO(0),
        IR(64),
        MAN(192);
        int _bitSum;
        Ride(int bitSum){
            _bitSum=bitSum;
        }

        private int getValue(){
            return _bitSum;
        }
    }

    private enum Acceleration {
        FORWARD_FAST(7,1000,24),
        FORWARD_NORMAL(5,7,16),
        FORWARD_SLOW(2,5,8),
        FORWARD_STILL(0,2,0),
        BACKWARDS_STILL(-2,0,0),
        BACKWARDS_SLOW(-5,-2,40),
        BACKWARDS_NORMAL(-7,-5,48),
        BACKWARDS_FAST(-1000,-7,56);

        private float _lowerBoundary,_upperBoundary;
        private int _value;

         Acceleration(float lowerBound ,float upperBound,int value) {
            _lowerBoundary=lowerBound;
            _upperBoundary=upperBound;
             _value=value;
         }

        public boolean isInBoundary(float gravity) {
            return (gravity>_lowerBoundary && gravity<_upperBoundary);
        }
        public static Acceleration getAcceleration(float gravity) {
            for(Acceleration acc : Acceleration.values()) {
                if(acc.isInBoundary(gravity)) return acc;
            }
            return null;
        }
        //GET
        public float getLowerBoundary() {
            return _lowerBoundary;
        }
        public float getUpperBoundary() {
            return _upperBoundary;
        }
        public int getValue() {
            return _value;
        }
    }

    private enum Rotation {
        LEFT_SHARP(4,1000,3),
        LEFT_NORMAL(2.5f,4,2),
        LEFT_WIDE(1.2f,2.5f,1),
        NONE(0,1.2f,0),
        NONERIGHT(-1.2f,0,0),
        RIGHT_WIDE(-2.5f,-1.2f,5),
        RIGHT_NORMAL(-4,-2.5f,6),
        RIGHT_SHARP(-1000,-4,7);

        private float _lowerBoundary,_upperBoundary;
        private int _value;

        Rotation(float lowerBound ,float upperBound,int value) {
            _lowerBoundary=lowerBound;
            _upperBoundary=upperBound;
            _value=value;
        }

        public boolean isInBoundary(float gravity) {
            return (gravity>_lowerBoundary && gravity<_upperBoundary);
        }
        public static Rotation getRotation(float gravity) {
            for(Rotation rot : Rotation.values()) {
                if(rot.isInBoundary(gravity)) return rot;
            }
            return null;
        }
        //GET
        public float getLowerBoundary() {
            return _lowerBoundary;
        }
        public float getUpperBoundary() {
            return _upperBoundary;
        }
        public int getValue() {
            return _value;
        }
    }

    public SensorController(SensorManager manager, BluetoothController bc){
        _manager = manager;
        _acceleroMeter = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        _bc=bc;
    }
    public void start() {
        _manager.registerListener(this, _acceleroMeter, SensorManager.SENSOR_DELAY_NORMAL);
        ride=Ride.MAN;
    }
    public void stop() {
        _manager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        _acceleration=Acceleration.getAcceleration(event.values[2]);
        _rotation=Rotation.getRotation(event.values[1]);
        Log.v("acceleratie : ",_acceleration.toString());
        Log.v("rotatie : ",_rotation.toString());

        int dataByte = _acceleration.getValue()|_rotation.getValue()|ride.getValue();
        Log.v("value :",""+dataByte);

        _bc.write(dataByte);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
