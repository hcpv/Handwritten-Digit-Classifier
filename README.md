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
