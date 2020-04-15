# Handwritten Digit Clasifier

Handwritten Digit Clasiifier is an android app that is used to classify handwritten digit. It uses a CNN model trained on [MNIST dataset](http://yann.lecun.com/exdb/mnist/) to classify. The accuracy of the model was 98.40%. 

## PRE-REQUISITES
* [python 3.7.7](https://www.python.org/downloads/)
* [jupyter notebook](https://jupyter.org/install)
* [keras](https://www.tensorflow.org/api_docs/python/tf/keras)
* [tensorflow lite](https://www.tensorflow.org/lite)
* [android studio 3.6.1](https://developer.android.com/studio)
* [opencv for android](https://sourceforge.net/projects/opencvlibrary/files/opencv-android/)

## STEPS
### a) Model training
**1)** Load the MNIST dataset
```python
(x_train, y_train), (x_test, y_test) = keras.datasets.mnist.load_data()
```
**2)** Building and compiling of the model
```python
model = Sequential()
model.add(Conv2D(16, kernel_size=3, activation='relu', input_shape=(28,28,1)))
model.add(MaxPooling2D(pool_size=(2, 2)))
model.add(Conv2D(32, kernel_size=3, activation='relu'))
model.add(MaxPooling2D(pool_size=(2, 2)))
model.add(Conv2D(64, kernel_size=3, activation='relu'))
model.add(MaxPooling2D(pool_size=(2, 2)))
model.add(Dropout(0.25))
model.add(Flatten())
model.add(Dense(10, activation='softmax'))
model.compile(optimizer='adam', loss='categorical_crossentropy', metrics=['accuracy'])
```
**3)** Train the model
```python
model.fit_generator(train_generator, validation_data=test_generator, epochs=10)
```
**4)** Saving the model
```python
model.save('mnist_cnn.h5')
```
### b) Conversion to tflite format
The [saved model](model/mnist_cnn.h5) cannot be directly used in android apps. Firstly, it needs to be optimized enough to reside within apps. TensorFlow Lite is the solution to enabling ML models within mobile apps.
```python
model = keras.models.load_model('mnist_cnn.h5')
converter = tf.lite.TFLiteConverter.from_keras_model(model)
converter.optimizations = [tf.lite.Optimize.DEFAULT]
tflite_quantized_model = converter.convert()
f = open('mnist.tflite', "wb")
f.write(tflite_quantized_model)
f.close()
```
### c) Build android app
**1)** JavaCameraView is used to capture image.
**2)** Captured Image is preprossed using opencv.
```kotlin
        Imgproc.cvtColor(mat2,mat3,Imgproc.COLOR_BGR2GRAY)
        Imgproc.GaussianBlur(mat3,mat2,Size(35.0,35.0),0.0)
        Imgproc.threshold(mat2,mat3,70.0,255.0,Imgproc.THRESH_BINARY_INV+Imgproc.THRESH_OTSU)
        Imgproc.resize(mat3,mat3,Size(28.0,28.0))
```
