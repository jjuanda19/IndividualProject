import pandas as pd
import re
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import LabelEncoder
from sklearn.feature_extraction.text import CountVectorizer, TfidfTransformer
from sklearn.naive_bayes import MultinomialNB
from sklearn.pipeline import Pipeline
import pickle

# Load the dataset
file_path = 'remainder_data_set.csv'  # Update this path
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
X_train, X_test, y_train, y_test = train_test_split(X, y_encoded, test_size=0.3, random_state=7)

# Set up and train the classification model
text_clf = Pipeline([
    ('vect', CountVectorizer()),
    ('tfidf', TfidfTransformer()),
    ('clf', MultinomialNB()),
])
text_clf.fit(X_train, y_train)

# Evaluate the model
print(f"Training accuracy: {text_clf.score(X_train, y_train)}")
print(f"Test accuracy: {text_clf.score(X_test, y_test)}")

# Example: Predicting new descriptions with reminder names
new_texts = [
             'Mail letter',
             'Do jog',
             'Buy a coffe with my friends ',
             'Buy carrots',
             'Meet jose at the park',
             'Do gym',
             'Renew netflix',
             "Hang out with my friends",
             'Dog vet visit',
             'Training at gym',
             
             ]

predicted_categories = text_clf.predict(new_texts)
predicted_labels = encoder.inverse_transform(predicted_categories)
print(f"Predictions: {predicted_labels}")



