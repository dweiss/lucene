/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.lucene.analysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.apache.lucene.tests.util.LuceneTestCase;
import org.apache.lucene.util.SuppressForbidden;

public class TestCharArraySet extends LuceneTestCase {

  static final String[] TEST_STOP_WORDS = {
    "a", "an", "and", "are", "as", "at", "be", "but", "by", "for", "if", "in", "into", "is", "it",
    "no", "not", "of", "on", "or", "such", "that", "the", "their", "then", "there", "these", "they",
    "this", "to", "was", "will", "with"
  };

  public void testRehash() throws Exception {
    CharArraySet cas = new CharArraySet(0, true);
    for (String stopWord : TEST_STOP_WORDS) {
      cas.add(stopWord);
    }
    assertEquals(TEST_STOP_WORDS.length, cas.size());
    for (String testStopWord : TEST_STOP_WORDS) {
      assertTrue(cas.contains(testStopWord));
    }
  }

  public void testNonZeroOffset() {
    String[] words = {"Hello", "World", "this", "is", "a", "test"};
    char[] findme = "xthisy".toCharArray();
    CharArraySet set = new CharArraySet(10, true);
    set.addAll(Arrays.asList(words));
    assertTrue(set.contains(findme, 1, 4));
    assertTrue(set.contains(new String(findme, 1, 4)));

    // test unmodifiable
    set = CharArraySet.unmodifiableSet(set);
    assertTrue(set.contains(findme, 1, 4));
    assertTrue(set.contains(new String(findme, 1, 4)));
  }

  @SuppressForbidden(reason = "Explicitly checking new Integers")
  public void testObjectContains() {
    CharArraySet set = new CharArraySet(10, true);
    Integer val = 1;
    set.add(val);
    assertTrue(set.contains(val));
    assertTrue(set.contains("1"));
    assertTrue(set.contains(new char[] {'1'}));
    // test unmodifiable
    set = CharArraySet.unmodifiableSet(set);
    assertTrue(set.contains(val));
    assertTrue(set.contains("1"));
    assertTrue(set.contains(new char[] {'1'}));
  }

  public void testClear() {
    CharArraySet set = new CharArraySet(10, true);
    set.addAll(Arrays.asList(TEST_STOP_WORDS));
    assertEquals("Not all words added", TEST_STOP_WORDS.length, set.size());
    set.clear();
    assertEquals("not empty", 0, set.size());
    for (String testStopWord : TEST_STOP_WORDS) {
      assertFalse(set.contains(testStopWord));
    }
    set.addAll(Arrays.asList(TEST_STOP_WORDS));
    assertEquals("Not all words added", TEST_STOP_WORDS.length, set.size());
    for (String testStopWord : TEST_STOP_WORDS) {
      assertTrue(set.contains(testStopWord));
    }
  }

  // TODO: break this up into simpler test methods, vs "telling a story"
  @SuppressWarnings("UnnecessaryStringBuilder")
  public void testModifyOnUnmodifiable() {
    CharArraySet set = new CharArraySet(10, true);
    set.addAll(Arrays.asList(TEST_STOP_WORDS));
    final int size = set.size();
    CharArraySet unmodifiableSet = CharArraySet.unmodifiableSet(set);
    assertEquals("Set size changed due to unmodifiableSet call", size, unmodifiableSet.size());
    String NOT_IN_SET = "SirGallahad";
    assertFalse("Test String already exists in set", unmodifiableSet.contains(NOT_IN_SET));

    expectThrows(
        UnsupportedOperationException.class, () -> unmodifiableSet.add(NOT_IN_SET.toCharArray()));
    assertFalse(
        "Test String has been added to unmodifiable set", unmodifiableSet.contains(NOT_IN_SET));
    assertEquals("Size of unmodifiable set has changed", size, unmodifiableSet.size());

    expectThrows(UnsupportedOperationException.class, () -> unmodifiableSet.add(NOT_IN_SET));
    assertFalse(
        "Test String has been added to unmodifiable set", unmodifiableSet.contains(NOT_IN_SET));
    assertEquals("Size of unmodifiable set has changed", size, unmodifiableSet.size());

    expectThrows(
        UnsupportedOperationException.class,
        () -> unmodifiableSet.add(new StringBuilder(NOT_IN_SET)));
    assertFalse(
        "Test String has been added to unmodifiable set", unmodifiableSet.contains(NOT_IN_SET));
    assertEquals("Size of unmodifiable set has changed", size, unmodifiableSet.size());

    expectThrows(UnsupportedOperationException.class, unmodifiableSet::clear);
    assertFalse("Changed unmodifiable set", unmodifiableSet.contains(NOT_IN_SET));
    assertEquals("Size of unmodifiable set has changed", size, unmodifiableSet.size());

    expectThrows(
        UnsupportedOperationException.class, () -> unmodifiableSet.add((Object) NOT_IN_SET));
    assertFalse(
        "Test String has been added to unmodifiable set", unmodifiableSet.contains(NOT_IN_SET));
    assertEquals("Size of unmodifiable set has changed", size, unmodifiableSet.size());

    // This test was changed in 3.1, as a contains() call on the given Collection using the
    // "correct" iterator's
    // current key (now a char[]) on a Set<String> would not hit any element of the CAS and therefor
    // never call
    // remove() on the iterator
    expectThrows(
        UnsupportedOperationException.class,
        () -> unmodifiableSet.removeAll(new CharArraySet(Arrays.asList(TEST_STOP_WORDS), true)));
    assertEquals("Size of unmodifiable set has changed", size, unmodifiableSet.size());

    expectThrows(
        UnsupportedOperationException.class,
        () -> unmodifiableSet.retainAll(new CharArraySet(Arrays.asList(NOT_IN_SET), true)));
    assertEquals("Size of unmodifiable set has changed", size, unmodifiableSet.size());

    expectThrows(
        UnsupportedOperationException.class,
        () -> unmodifiableSet.addAll(Arrays.asList(NOT_IN_SET)));
    assertFalse(
        "Test String has been added to unmodifiable set", unmodifiableSet.contains(NOT_IN_SET));

    for (String testStopWord : TEST_STOP_WORDS) {
      assertTrue(set.contains(testStopWord));
      assertTrue(unmodifiableSet.contains(testStopWord));
    }
  }

  public void testUnmodifiableSet() {
    CharArraySet set = new CharArraySet(10, true);
    set.addAll(Arrays.asList(TEST_STOP_WORDS));
    set.add(1);
    final int size = set.size();
    set = CharArraySet.unmodifiableSet(set);
    assertEquals("Set size changed due to unmodifiableSet call", size, set.size());
    for (String stopword : TEST_STOP_WORDS) {
      assertTrue(set.contains(stopword));
    }
    assertTrue(set.contains(1));
    assertTrue(set.contains("1"));
    assertTrue(set.contains(new char[] {'1'}));

    expectThrows(NullPointerException.class, () -> CharArraySet.unmodifiableSet(null));
  }

  public void testSupplementaryChars() {
    String missing = "Term %s is missing in the set";
    String falsePos = "Term %s is in the set but shouldn't";
    // for reference see
    // http://unicode.org/cldr/utility/list-unicodeset.jsp?a=[[%3ACase_Sensitive%3DTrue%3A]%26[^[\u0000-\uFFFF]]]&esc=on
    String[] upperArr =
        new String[] {"Abc\ud801\udc1c", "\ud801\udc1c\ud801\udc1cCDE", "A\ud801\udc1cB"};
    String[] lowerArr =
        new String[] {"abc\ud801\udc44", "\ud801\udc44\ud801\udc44cde", "a\ud801\udc44b"};
    CharArraySet set = new CharArraySet(Arrays.asList(TEST_STOP_WORDS), true);
    Collections.addAll(set, upperArr);
    for (int i = 0; i < upperArr.length; i++) {
      assertTrue(String.format(Locale.ROOT, missing, upperArr[i]), set.contains(upperArr[i]));
      assertTrue(String.format(Locale.ROOT, missing, lowerArr[i]), set.contains(lowerArr[i]));
    }
    set = new CharArraySet(Arrays.asList(TEST_STOP_WORDS), false);
    Collections.addAll(set, upperArr);
    for (int i = 0; i < upperArr.length; i++) {
      assertTrue(String.format(Locale.ROOT, missing, upperArr[i]), set.contains(upperArr[i]));
      assertFalse(String.format(Locale.ROOT, falsePos, lowerArr[i]), set.contains(lowerArr[i]));
    }
  }

  public void testSingleHighSurrogate() {
    String missing = "Term %s is missing in the set";
    String falsePos = "Term %s is in the set but shouldn't";
    String[] upperArr =
        new String[] {"ABC\uD800", "ABC\uD800EfG", "\uD800EfG", "\uD800\ud801\udc1cB"};

    String[] lowerArr =
        new String[] {"abc\uD800", "abc\uD800efg", "\uD800efg", "\uD800\ud801\udc44b"};
    CharArraySet set = new CharArraySet(Arrays.asList(TEST_STOP_WORDS), true);
    Collections.addAll(set, upperArr);
    for (int i = 0; i < upperArr.length; i++) {
      assertTrue(String.format(Locale.ROOT, missing, upperArr[i]), set.contains(upperArr[i]));
      assertTrue(String.format(Locale.ROOT, missing, lowerArr[i]), set.contains(lowerArr[i]));
    }
    set = new CharArraySet(Arrays.asList(TEST_STOP_WORDS), false);
    Collections.addAll(set, upperArr);
    for (int i = 0; i < upperArr.length; i++) {
      assertTrue(String.format(Locale.ROOT, missing, upperArr[i]), set.contains(upperArr[i]));
      assertFalse(String.format(Locale.ROOT, falsePos, upperArr[i]), set.contains(lowerArr[i]));
    }
  }

  public void testCopyCharArraySetBWCompat() {
    CharArraySet setIgnoreCase = new CharArraySet(10, true);
    CharArraySet setCaseSensitive = new CharArraySet(10, false);

    List<String> stopwords = Arrays.asList(TEST_STOP_WORDS);
    List<String> stopwordsUpper = new ArrayList<>();
    for (String string : stopwords) {
      stopwordsUpper.add(string.toUpperCase(Locale.ROOT));
    }
    setIgnoreCase.addAll(Arrays.asList(TEST_STOP_WORDS));
    setIgnoreCase.add(1);
    setCaseSensitive.addAll(Arrays.asList(TEST_STOP_WORDS));
    setCaseSensitive.add(1);

    CharArraySet copy = CharArraySet.copy(setIgnoreCase);
    CharArraySet copyCaseSens = CharArraySet.copy(setCaseSensitive);

    assertEquals(setIgnoreCase.size(), copy.size());
    assertEquals(setCaseSensitive.size(), copy.size());

    assertTrue(copy.containsAll(stopwords));
    assertTrue(copy.containsAll(stopwordsUpper));
    assertTrue(copyCaseSens.containsAll(stopwords));
    for (String string : stopwordsUpper) {
      assertFalse(copyCaseSens.contains(string));
    }
    // test adding terms to the copy
    List<String> newWords = new ArrayList<>();
    for (String string : stopwords) {
      newWords.add(string + "_1");
    }
    copy.addAll(newWords);

    assertTrue(copy.containsAll(stopwords));
    assertTrue(copy.containsAll(stopwordsUpper));
    assertTrue(copy.containsAll(newWords));
    // new added terms are not in the source set
    for (String string : newWords) {
      assertFalse(setIgnoreCase.contains(string));
      assertFalse(setCaseSensitive.contains(string));
    }
  }

  /** Test the static #copy() function with a CharArraySet as a source */
  public void testCopyCharArraySet() {
    CharArraySet setIgnoreCase = new CharArraySet(10, true);
    CharArraySet setCaseSensitive = new CharArraySet(10, false);

    List<String> stopwords = Arrays.asList(TEST_STOP_WORDS);
    List<String> stopwordsUpper = new ArrayList<>();
    for (String string : stopwords) {
      stopwordsUpper.add(string.toUpperCase(Locale.ROOT));
    }
    setIgnoreCase.addAll(Arrays.asList(TEST_STOP_WORDS));
    setIgnoreCase.add(1);
    setCaseSensitive.addAll(Arrays.asList(TEST_STOP_WORDS));
    setCaseSensitive.add(1);

    CharArraySet copy = CharArraySet.copy(setIgnoreCase);
    CharArraySet copyCaseSens = CharArraySet.copy(setCaseSensitive);

    assertEquals(setIgnoreCase.size(), copy.size());
    assertEquals(setCaseSensitive.size(), copy.size());

    assertTrue(copy.containsAll(stopwords));
    assertTrue(copy.containsAll(stopwordsUpper));
    assertTrue(copyCaseSens.containsAll(stopwords));
    for (String string : stopwordsUpper) {
      assertFalse(copyCaseSens.contains(string));
    }
    // test adding terms to the copy
    List<String> newWords = new ArrayList<>();
    for (String string : stopwords) {
      newWords.add(string + "_1");
    }
    copy.addAll(newWords);

    assertTrue(copy.containsAll(stopwords));
    assertTrue(copy.containsAll(stopwordsUpper));
    assertTrue(copy.containsAll(newWords));
    // new added terms are not in the source set
    for (String string : newWords) {
      assertFalse(setIgnoreCase.contains(string));
      assertFalse(setCaseSensitive.contains(string));
    }
  }

  /** Test the static #copy() function with a JDK {@link Set} as a source */
  public void testCopyJDKSet() {
    List<String> stopwords = Arrays.asList(TEST_STOP_WORDS);
    List<String> stopwordsUpper = new ArrayList<>();
    for (String string : stopwords) {
      stopwordsUpper.add(string.toUpperCase(Locale.ROOT));
    }
    Set<String> set = new HashSet<>(Arrays.asList(TEST_STOP_WORDS));

    CharArraySet copy = CharArraySet.copy(set);

    assertEquals(set.size(), copy.size());
    assertEquals(set.size(), copy.size());

    assertTrue(copy.containsAll(stopwords));
    for (String string : stopwordsUpper) {
      assertFalse(copy.contains(string));
    }

    List<String> newWords = new ArrayList<>();
    for (String string : stopwords) {
      newWords.add(string + "_1");
    }
    copy.addAll(newWords);

    assertTrue(copy.containsAll(stopwords));
    assertTrue(copy.containsAll(newWords));
    // new added terms are not in the source set
    for (String string : newWords) {
      assertFalse(set.contains(string));
    }
  }

  /**
   * Tests a special case of {@link CharArraySet#copy(Set)} where the set to copy is the {@link
   * CharArraySet#EMPTY_SET}
   */
  public void testCopyEmptySet() {
    assertSame(CharArraySet.EMPTY_SET, CharArraySet.copy(CharArraySet.EMPTY_SET));
  }

  /** Smoketests the static empty set */
  public void testEmptySet() {
    assertEquals(0, CharArraySet.EMPTY_SET.size());

    assertTrue(CharArraySet.EMPTY_SET.isEmpty());
    for (String stopword : TEST_STOP_WORDS) {
      assertFalse(CharArraySet.EMPTY_SET.contains(stopword));
    }
    assertFalse(CharArraySet.EMPTY_SET.contains("foo"));
    assertFalse(CharArraySet.EMPTY_SET.contains((Object) "foo"));
    assertFalse(CharArraySet.EMPTY_SET.contains("foo".toCharArray()));
    assertFalse(CharArraySet.EMPTY_SET.contains("foo".toCharArray(), 0, 3));
  }

  /** Test for NPE */
  public void testContainsWithNull() {
    CharArraySet set = new CharArraySet(1, true);

    expectThrows(NullPointerException.class, () -> set.contains(null, 0, 10));

    expectThrows(NullPointerException.class, () -> set.contains((CharSequence) null));

    expectThrows(NullPointerException.class, () -> set.contains((Object) null));
  }

  public void testToString() {
    CharArraySet set = CharArraySet.copy(Collections.singleton("test"));
    assertEquals("[test]", set.toString());
    set.add("test2");
    assertTrue(set.toString().contains(", "));
  }
}
