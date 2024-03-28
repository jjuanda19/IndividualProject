from lib2to3.pytree import convert
import pandas as pd
from sklearn.model_selection import train_test_split
import re
from tensorflow.keras.preprocessing.text import Tokenizer
from tensorflow.keras.preprocessing.sequence import pad_sequences
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import Embedding, GlobalAveragePooling1D, Dense, LSTM, Dropout, Bidirectional
from tensorflow.keras.utils import to_categorical
from sklearn.preprocessing import LabelEncoder
from tensorflow.keras.callbacks import EarlyStopping
import tensorflow as tf
import numpy as np
from tensorflow import keras
#from tensorflow.contrib import lite
import os


# Load the dataset
file_path = 'remainder_data_set.csv'  
data = pd.read_csv(file_path)

# Basic text cleaning function
def basic_clean_text(text):
    text = re.sub(r'[^\w\s]', '', text)  # Remove punctuation
    text = text.lower()  # Convert to lowercase
    words = text.split()  # Split into words
    words = [word for word in words if len(word) > 2]  # Remove short words
    return ' '.join(words)

# Combine reminder_name and description, then clean the combined text
data['combined_text'] = data['reminder_name'] + ' ' + data['description']
data['clean_combined_text'] = data['combined_text'].apply(basic_clean_text)

# Prepare features and labels
X = data['clean_combined_text']
y = data['tag']

# Encode the labels
encoder = LabelEncoder()
y_encoded = encoder.fit_transform(y)


# Split the dataset
X_train, X_test, y_train, y_test = train_test_split(X, y_encoded, test_size=0.3, random_state=6)

# Tokenize the text
tokenizer = Tokenizer(num_words=10000, oov_token="<OOV>")
tokenizer.fit_on_texts(X_train)
X_train_sequences = tokenizer.texts_to_sequences(X_train)
X_test_sequences = tokenizer.texts_to_sequences(X_test)

# Pad the sequences
max_length = max(len(x) for x in X_train_sequences)
X_train_padded = pad_sequences(X_train_sequences, maxlen=max_length, padding='post')
X_test_padded = pad_sequences(X_test_sequences, maxlen=max_length, padding='post')


# Prepare categorical labels
y_train_categorical = to_categorical(y_train)
y_test_categorical = to_categorical(y_test)


model = Sequential([
    Embedding(input_dim=1000, output_dim=64),  # Increase output dimensions
    Bidirectional(LSTM(50, return_sequences=True)),
    GlobalAveragePooling1D(),
    Dense(50, activation='relu'),  # Increase number of neurons
    Dropout(0.5),  # Add dropout for regularization
    Dense(len(encoder.classes_), activation='softmax')
])


model.compile(optimizer='adam', loss='categorical_crossentropy', metrics=['accuracy'])



# Implement early stopping
early_stopping = EarlyStopping(monitor='val_loss', patience=3)

# Train the model with early stopping
model.fit(X_train_padded, y_train_categorical,
          epochs=20,
          validation_data=(X_test_padded, y_test_categorical),
          verbose=2,
          callbacks=[early_stopping])
# Evaluate the model
loss, accuracy = model.evaluate(X_test_padded, y_test_categorical, verbose=2)
print(f"Test accuracy: {accuracy}")
# Prepare a single text for prediction
texts = ['Mail letter',
             'Do jog',
             'Buy a coffe with my friends ',
             'Buy carrots',
             'Meet jose at the park',
             'Do gym',
             'Renew netflix',
             "Hang out with my friends",
             'Dog vet visit',
             'Training at gym']

input_data = pad_sequences(tokenizer.texts_to_sequences([basic_clean_text(text) for text in texts]), maxlen=max_length, padding='post')

# Predict and print the label for the text
predicted_class_indices = np.argmax(model.predict(input_data), axis=1)
predicted_labels = encoder.inverse_transform(predicted_class_indices)
print(f"Text: {texts} - Predicted label: {predicted_labels}")





