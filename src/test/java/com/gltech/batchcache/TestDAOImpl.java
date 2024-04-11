package com.gltech.batchcache;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TestDAOImpl implements TestDAO
{
    @Override
    @BatchCache(key = "all-companies")
    public List<TestCompany> getAllCompanies()
    {
        return List.of(new TestCompany(1L, "tc1"), new TestCompany(2L, "tc2"), new TestCompany(3L, "tc3"));
    }

    @Override
    @BatchCacheEvict(key = "all-companies")
    public void clearAllCompanies()
    {
    }

    @Override
    @BatchCache(key = "company")
    public TestCompany getCompany(int id)
    {
        return new TestCompany(id, "Super Company " + id);
    }

    @Override
    @BatchCache(key = "company")
    public TestCompany getCompany(Integer id)
    {
        return new TestCompany(id, "Super Company " + id);
    }

    @Override
    @BatchCache(key = "company")
    public TestCompany getCompany(long id)
    {
        return new TestCompany(id, "Super Company " + id);
    }

    @Override
    @BatchCache(key = "company")
    public TestCompany getCompany(Long id)
    {
        return new TestCompany(id, "Super Company " + id);
    }

    @Override
    @BatchCache(key = "company")
    public TestCompany getCompany(String id)
    {
        return new TestCompany(Long.parseLong(id), "Super Company " + id);
    }

    @Override
    @BatchCache(key = "test")
    public TestCompany getNullParameter(String test)
    {
        return null;
    }

    @Override
    @BatchCache(key = "transform", field = "someId")
    public TestObjectAfter transformObjectMatchingId(TestObjectBefore testObject)
    {
        return new TestObjectAfter(testObject.getSomeId());
    }

    @Override
    @BatchCache(key = "transform", field = "someId")
    public TestCompany transformObjectMismatchingId(TestObjectBefore testObject)
    {
        return new TestCompany(testObject.getSomeId(), "Test Company " + testObject.getSomeId());
    }

    @Override
    @BatchCache(key = "transform", field = "someId")
    public List<TestObjectAfter> transformObjectMatchingIds(Collection<TestObjectBefore> testObjects)
    {
        return testObjects.stream().map(test -> new TestObjectAfter(test.getSomeId())).collect(Collectors.toList());
    }

    @Override
    @BatchCache(key = "transform", field = "someId")
    public List<TestCompany> transformObjectMismatchingIds(Collection<TestObjectBefore> testObjects)
    {
        return testObjects.stream().map(test -> new TestCompany(test.getSomeId(), "Test Company "+ test.getSomeId())).collect(Collectors.toList());
    }

    @Override
    @BatchCache(key = "dateobj")
    public TestDateObj getDateObj(Date date)
    {
        return new TestDateObj(date, "test date");
    }

    @Override
    @BatchCache(key = "company")
    public List<TestCompany> getCompanies(int[] ids)
    {
        return Arrays.stream(ids).mapToObj(id -> new TestCompany(id, "Super Company " + id)).collect(Collectors.toList());
    }

    @Override
    @BatchCache(key = "company")
    public Set<TestCompany> getCompanies(Integer[] ids)
    {
        return Arrays.stream(ids).map(id -> new TestCompany(id, "Super Company " + id)).collect(Collectors.toSet());
    }

    @Override
    @BatchCache(key = "company")
    public List<TestCompany> getCompaniesInteger(Collection<Integer> ids)
    {
        return ids.stream().map(id -> new TestCompany(id, "Super Company " + id)).collect(Collectors.toList());
    }

    @Override
    @BatchCache(key = "company")
    public Set<TestCompany> getCompanies(long[] ids)
    {
        return Arrays.stream(ids).mapToObj(id -> new TestCompany(id, "Super Company " + id)).collect(Collectors.toSet());
    }

    @Override
    @BatchCache(key = "company")
    public List<TestCompany> getCompanies(Long[] ids)
    {
        return Arrays.stream(ids).map(id -> new TestCompany(id, "Super Company " + id)).collect(Collectors.toList());
    }

    @Override
    @BatchCache(key = "company")
    public List<TestCompany> getCompaniesLong(Collection<Long> ids)
    {
        return ids.stream().map(id -> new TestCompany(id, "Super Company " + id)).collect(Collectors.toList());
    }

    @Override
    @BatchCache(key = "company")
    public List<TestCompany> getCompanies(String[] ids)
    {
        return Arrays.stream(ids).map(id -> new TestCompany(Long.parseLong(id), "Super Company " + id)).collect(Collectors.toList());
    }

    @Override
    @BatchCache(key = "company")
    public Set<TestCompany> getCompaniesString(Collection<String> ids)
    {
        return ids.stream().map(id -> new TestCompany(Long.parseLong(id), "Super Company " + id)).collect(Collectors.toSet());
    }

    @Override
    @BatchCache(key = "company")
    public Map<Integer, TestCompany> getCompaniesMap(int[] ids)
    {
        return Arrays.stream(ids).mapToObj(id -> new TestCompany(id, "Super Company " + id)).collect(Collectors.toMap(tc -> (int)tc.getId(), Function.identity()));
    }

    @Override
    @BatchCache(key = "company")
    public Map<Integer, List<TestCompany>> getCompaniesMapList(int[] ids)
    {
        return Arrays.stream(ids).mapToObj(id -> new TestCompany(id, "Super Company " + id)).collect(Collectors.toMap(tc -> (int)tc.getId(), List::of));
    }

    @Override
    @BatchCache(key = "company")
    public Map<Integer, TestCompany> getCompaniesMap(Integer[] ids)
    {
        return Arrays.stream(ids).map(id -> new TestCompany(id, "Super Company " + id)).collect(Collectors.toMap(tc -> (int)tc.getId(), Function.identity()));
    }

    @Override
    @BatchCache(key = "company")
    public Map<Integer, TestCompany> getCompaniesMapInteger(Collection<Integer> ids)
    {
        return ids.stream().map(id -> new TestCompany(id, "Super Company " + id)).collect(Collectors.toMap(tc -> (int)tc.getId(), Function.identity()));
    }

    @Override
    @BatchCache(key = "company")
    public Map<Long, TestCompany> getCompaniesMap(long[] ids)
    {
        return Arrays.stream(ids).mapToObj(id -> new TestCompany(id, "Super Company " + id)).collect(Collectors.toMap(TestCompany::getId, Function.identity()));
    }

    @Override
    @BatchCache(key = "company")
    public Map<Long, TestCompany> getCompaniesMap(Long[] ids)
    {
        return Arrays.stream(ids).map(id -> new TestCompany(id, "Super Company " + id)).collect(Collectors.toMap(TestCompany::getId, Function.identity()));
    }

    @Override
    @BatchCache(key = "company")
    public Map<Long, TestCompany> getCompaniesMapLong(Collection<Long> ids)
    {
        return ids.stream().map(id -> new TestCompany(id, "Super Company " + id)).collect(Collectors.toMap(TestCompany::getId, Function.identity()));
    }

    @Override
    @BatchCache(key = "company")
    public Map<String, TestCompany> getCompaniesMap(String[] ids)
    {
        return Arrays.stream(ids).map(id -> new TestCompany(Long.parseLong(id), "Super Company " + id)).collect(Collectors.toMap(tc -> String.valueOf(tc.getId()), Function.identity()));
    }

    @Override
    @BatchCache(key = "company")
    public Map<String, TestCompany> getCompaniesMapString(Collection<String> ids)
    {
        return ids.stream().map(id -> new TestCompany(Long.parseLong(id), "Super Company " + id)).collect(Collectors.toMap(tc -> String.valueOf(tc.getId()), Function.identity()));
    }

    @Override
    @BatchCache(key = "transform", field = "someId")
    public Map<TestObjectBefore, TestCompany> transformObjectMismatchingIdsMap(Collection<TestObjectBefore> testObjects)
    {
        return testObjects.stream().collect(Collectors.toMap(Function.identity(), test -> new TestCompany(test.getSomeId(), "Super Company "+ test.getSomeId())));
    }

    @Override
    @BatchCache(key = "transform", field = "someId")
    public Map<TestObjectBefore, TestObjectAfter> transformObjectMatchingIdsMap(Collection<TestObjectBefore> testObjects)
    {
        return testObjects.stream().collect(Collectors.toMap(Function.identity(), test -> new TestObjectAfter(test.getSomeId())));
    }

    @Override
    @BatchCacheEvict(key = "company")
    public void save(TestCompany company)
    {
    }

    @Override
    @BatchCacheEvict(key = "company")
    public void save(Collection<TestCompany> company)
    {
    }

    @Override
    @BatchCacheEvict(key = "dateobj", field = "date")
    public void save(TestDateObj dateObj)
    {
    }

    @Override
    @BatchCacheEvict(key = "company")
    public void delete(int id)
    {
    }

    @Override
    @BatchCacheEvict(key = "company")
    public void delete(int id, int someIgnoredId)
    {
    }

    @Override
    @BatchCacheEvict(key = "company")
    public void refresh(Integer id)
    {
    }

    @Override
    @BatchCacheEvict(key = "company")
    public void delete(int[] ids)
    {
    }

    @Override
    @BatchCacheEvict(key = "company")
    public void refresh(Integer[] ids)
    {
    }

    @Override
    @BatchCacheEvict(key = "company")
    public void clearIntegers(Collection<Integer> ids)
    {
    }

    @Override
    @BatchCacheEvict(key = "company")
    public void delete(long id)
    {
    }

    @Override
    @BatchCacheEvict(key = "company")
    public void refresh(Long id)
    {
    }

    @Override
    @BatchCacheEvict(key = "company")
    public void delete(long[] ids)
    {
    }

    @Override
    @BatchCacheEvict(key = "company")
    public void refresh(Long[] ids)
    {
    }

    @Override
    @BatchCacheEvict(key = "company")
    public void clearLongs(Collection<Long> ids)
    {
    }

    @Override
    @BatchCacheEvict(key = "company")
    public void delete(String id)
    {
    }

    @Override
    @BatchCacheEvict(key = "company")
    public void refresh(String[] ids)
    {
    }

    @Override
    @BatchCacheEvict(key = "company")
    public void clearStrings(Collection<String> ids)
    {
    }

    @Override
    @BatchCacheEvict(key = "company")
    public void clearTestCompanies(Collection<TestCompany> testCompanies)
    {
    }

    @Override
    @BatchCacheEvict(key = "transform", field = "someId")
    public void clearTransformation(TestObjectBefore testObject)
    {
    }

    @Override
    @BatchCacheEvict(key = "transform", field = "someId")
    public void clearTransformations(Collection<TestObjectBefore> testObjects)
    {
    }

    @Override
    @BatchCacheEvict(key = "company,testobject")
    public void evictMultipleKeys(int id)
    {
    }

    @Override
    @BatchCacheEvict(key = "testobject-a,testobject-b", field = "someId")
    public void evictMultipleKeys(TestObjectBefore testObject)
    {
    }

    @Override
    @BatchCacheEvict(key = "testobject-a,testobject-b", field = "someId")
    public void evictMultipleKeys(Collection<TestObjectBefore> testObjects)
    {
    }

    @Override
    @BatchCacheEvict(key = "test", field = "someId")
    public void handleNull(String nullParameter)
    {
    }

    static public class TestCompany
    {
        private final long id;
        private final String name;

        public TestCompany()
        {
            this.id = 0;
            this.name = null;
        }

        public TestCompany(long id, String name)
        {
            this.id = id;
            this.name = name;
        }

        public long getId()
        {
            return id;
        }

        public String getName()
        {
            return name;
        }
    }

    static public class TestDateObj
    {
        private final Date date;
        private final String name;

        public TestDateObj()
        {
            this.date = null;
            this.name = null;
        }

        public TestDateObj(Date date, String name)
        {
            this.date = date;
            this.name = name;
        }

        public Date getDate()
        {
            return date;
        }

        public String getName()
        {
            return name;
        }
    }

    static public class TestObjectBefore
    {
        private final long someId;

        public TestObjectBefore()
        {
            this.someId = 0;
        }

        public TestObjectBefore(long someId)
        {
            this.someId = someId;
        }

        public long getSomeId()
        {
            return someId;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestObjectBefore that = (TestObjectBefore) o;
            return someId == that.someId;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(someId);
        }
    }

    static public class TestObjectAfter
    {
        private final long someId;

        public TestObjectAfter()
        {
            this.someId = 0;
        }

        public TestObjectAfter(long someId)
        {
            this.someId = someId;
        }

        public long getSomeId()
        {
            return someId;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestObjectBefore that = (TestObjectBefore) o;
            return someId == that.someId;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(someId);
        }
    }
}

