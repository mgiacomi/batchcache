package com.gltech.batchcache;

import com.gltech.batchcache.TestDAOImpl.TestCompany;
import com.gltech.batchcache.TestDAOImpl.TestDateObj;
import com.gltech.batchcache.TestDAOImpl.TestObjectAfter;
import com.gltech.batchcache.TestDAOImpl.TestObjectBefore;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class BatchCacheEvictTests
{
    private CacheClientImpl cacheClient;
    private TestDAO testDAO;

    @Before
    public void setUp()
    {
        cacheClient = new CacheClientImpl();

        BatchCacheAspect batchCacheAspect = new BatchCacheAspect();
        ReflectionTestUtils.setField(batchCacheAspect, "cacheClient", cacheClient);

        BatchCacheEvictAspect batchCacheEvictAspect = new BatchCacheEvictAspect();
        ReflectionTestUtils.setField(batchCacheEvictAspect, "cacheClient", cacheClient);

        TestDAO testDAOImpl = new TestDAOImpl();
        AspectJProxyFactory factory = new AspectJProxyFactory(testDAOImpl);
        factory.addAspect(batchCacheAspect);
        factory.addAspect(batchCacheEvictAspect);

        testDAO = factory.getProxy();
    }

    @After
    public void tearDown()
    {
        cacheClient.clearAll();
        cacheClient = null;
        testDAO = null;
    }

    @Test
    public void evictAll()
    {
        List<TestCompany> companies = testDAO.getAllCompanies();
        assertNotNull(cacheClient.get("all-companies"));
        testDAO.clearAllCompanies();
        assertNull(cacheClient.get("all-companies"));
    }

    @Test
    public void evictMultipleKeys()
    {
        cacheClient.set("company-1", "c1");
        cacheClient.set("testobject-1", "to1");
        assertNotNull(cacheClient.get("company-1"));
        assertNotNull(cacheClient.get("testobject-1"));
        testDAO.evictMultipleKeys(1);
        assertNull(cacheClient.get("company-1"));
        assertNull(cacheClient.get("testobject-1"));
    }

    @Test
    public void evictMultipleKeysObject()
    {
        cacheClient.set("testobject-a-13", "toa13");
        cacheClient.set("testobject-b-13", "tob13");
        assertNotNull(cacheClient.get("testobject-a-13"));
        assertNotNull(cacheClient.get("testobject-b-13"));
        testDAO.evictMultipleKeys(new TestObjectBefore(13));
        assertNull(cacheClient.get("testobject-a-13"));
        assertNull(cacheClient.get("testobject-b-13"));
    }

    @Test
    public void evictMultipleKeysCollection()
    {
        cacheClient.set("testobject-a-13", "toa13");
        cacheClient.set("testobject-a-14", "toa14");
        cacheClient.set("testobject-b-13", "tob13");
        cacheClient.set("testobject-b-14", "tob14");
        assertNotNull(cacheClient.get("testobject-a-13"));
        assertNotNull(cacheClient.get("testobject-b-14"));
        testDAO.evictMultipleKeys(List.of(new TestObjectBefore(13), new TestObjectBefore(14)));
        assertNull(cacheClient.get("testobject-a-13"));
        assertNull(cacheClient.get("testobject-a-14"));
        assertNull(cacheClient.get("testobject-b-13"));
        assertNull(cacheClient.get("testobject-b-14"));
    }

    @Test
    public void testNullParameters()
    {
        TestCompany company = testDAO.getNullParameter(null);
        assertNull(cacheClient.get("test"));
    }

    @Test
    public void saveObject()
    {
        TestCompany company = testDAO.getCompany(1);
        assertNotNull(cacheClient.get("company-1"));
        testDAO.save(company);
        assertNull(cacheClient.get("company-1"));
    }

    @Test
    public void saveDate() throws Exception
    {
        Date date = new SimpleDateFormat("MM/dd/yyyy").parse("2/1/2023");

        TestDateObj dateObj = testDAO.getDateObj(date);
        assertNotNull(cacheClient.get("dateobj-" + date.getTime()));
        testDAO.save(dateObj);
        assertNull(cacheClient.get("dateobj" + date.getTime()));
    }

    @Test
    public void saveObjectCollection()
    {
        List<TestCompany> companies = testDAO.getCompanies(new int[]{1, 2, 3});
        assertNotNull(cacheClient.get("company-1"));
        assertNotNull(cacheClient.get("company-2"));
        assertNotNull(cacheClient.get("company-3"));
        testDAO.save(companies);
        assertNull(cacheClient.get("company-1"));
        assertNull(cacheClient.get("company-2"));
        assertNull(cacheClient.get("company-3"));
    }

    @Test
    public void deleteById()
    {
        testDAO.getCompany(1);
        assertNotNull(cacheClient.get("company-1"));
        testDAO.delete(1);
        assertNull(cacheClient.get("company-1"));
    }

    @Test
    public void deleteByIdWithExtraParameters()
    {
        testDAO.getCompany(1);
        testDAO.getCompany(9);
        assertNotNull(cacheClient.get("company-1"));
        assertNotNull(cacheClient.get("company-9"));
        testDAO.delete(1, 9);
        assertNull(cacheClient.get("company-1"));
        assertNotNull(cacheClient.get("company-9"));
    }

    @Test
    public void deleteByLong()
    {
        TestCompany company = testDAO.getCompany(1);
        assertNotNull(cacheClient.get("company-1"));
        testDAO.delete(company.getId());
        assertNull(cacheClient.get("company-1"));
    }

    @Test
    public void deleteByString()
    {
        TestCompany company = testDAO.getCompany(1);
        assertNotNull(cacheClient.get("company-1"));
        testDAO.delete("1");
        assertNull(cacheClient.get("company-1"));
    }

    @Test
    public void refreshByInt()
    {
        TestCompany company = testDAO.getCompany(1);
        assertNotNull(cacheClient.get("company-1"));
        testDAO.refresh(Integer.valueOf(1));
        assertNull(cacheClient.get("company-1"));
    }

    @Test
    public void refreshByLong()
    {
        TestCompany company = testDAO.getCompany(1);
        assertNotNull(cacheClient.get("company-1"));
        testDAO.refresh(Long.valueOf(1));
        assertNull(cacheClient.get("company-1"));
    }

    @Test
    public void deleteIntArray()
    {
        List<TestCompany> companies = testDAO.getCompanies(new int[]{1, 2, 3});
        assertNotNull(cacheClient.get("company-1"));
        assertNotNull(cacheClient.get("company-2"));
        assertNotNull(cacheClient.get("company-3"));
        testDAO.delete(new int[]{1, 2, 3});
        assertNull(cacheClient.get("company-1"));
        assertNull(cacheClient.get("company-2"));
        assertNull(cacheClient.get("company-3"));
    }

    @Test
    public void deleteLongArray()
    {
        List<TestCompany> companies = testDAO.getCompanies(new int[]{1, 2, 3});
        assertNotNull(cacheClient.get("company-1"));
        assertNotNull(cacheClient.get("company-2"));
        assertNotNull(cacheClient.get("company-3"));
        testDAO.delete(new long[]{1L, 2L, 3L});
        assertNull(cacheClient.get("company-1"));
        assertNull(cacheClient.get("company-2"));
        assertNull(cacheClient.get("company-3"));
    }

    @Test
    public void refreshIntegerArray()
    {
        List<TestCompany> companies = testDAO.getCompanies(new int[]{1, 2, 3});
        assertNotNull(cacheClient.get("company-1"));
        assertNotNull(cacheClient.get("company-2"));
        assertNotNull(cacheClient.get("company-3"));
        testDAO.refresh(new Integer[]{1, 2, 3});
        assertNull(cacheClient.get("company-1"));
        assertNull(cacheClient.get("company-2"));
        assertNull(cacheClient.get("company-3"));
    }

    @Test
    public void refreshLongArray()
    {
        List<TestCompany> companies = testDAO.getCompanies(new int[]{1, 2, 3});
        assertNotNull(cacheClient.get("company-1"));
        assertNotNull(cacheClient.get("company-2"));
        assertNotNull(cacheClient.get("company-3"));
        testDAO.refresh(new Long[]{1L, 2L, 3L});
        assertNull(cacheClient.get("company-1"));
        assertNull(cacheClient.get("company-2"));
        assertNull(cacheClient.get("company-3"));
    }

    @Test
    public void refreshStringArray()
    {
        List<TestCompany> companies = testDAO.getCompanies(new int[]{1, 2, 3});
        assertNotNull(cacheClient.get("company-1"));
        assertNotNull(cacheClient.get("company-2"));
        assertNotNull(cacheClient.get("company-3"));
        testDAO.refresh(new String[]{"1", "2", "3"});
        assertNull(cacheClient.get("company-1"));
        assertNull(cacheClient.get("company-2"));
        assertNull(cacheClient.get("company-3"));
    }

    @Test
    public void clearIntegerList()
    {
        List<TestCompany> companies = testDAO.getCompanies(new int[]{1, 2, 3});
        assertNotNull(cacheClient.get("company-1"));
        assertNotNull(cacheClient.get("company-2"));
        assertNotNull(cacheClient.get("company-3"));
        testDAO.clearIntegers(List.of(1, 2, 3));
        assertNull(cacheClient.get("company-1"));
        assertNull(cacheClient.get("company-2"));
        assertNull(cacheClient.get("company-3"));
    }

    @Test
    public void clearLongList()
    {
        List<TestCompany> companies = testDAO.getCompanies(new int[]{1, 2, 3});
        assertNotNull(cacheClient.get("company-1"));
        assertNotNull(cacheClient.get("company-2"));
        assertNotNull(cacheClient.get("company-3"));
        testDAO.clearLongs(List.of(1L, 2L, 3L));
        assertNull(cacheClient.get("company-1"));
        assertNull(cacheClient.get("company-2"));
        assertNull(cacheClient.get("company-3"));
    }

    @Test
    public void clearStringList()
    {
        List<TestCompany> companies = testDAO.getCompanies(new int[]{1, 2, 3});
        assertNotNull(cacheClient.get("company-1"));
        assertNotNull(cacheClient.get("company-2"));
        assertNotNull(cacheClient.get("company-3"));
        testDAO.clearStrings(List.of("1", "2", "3"));
        assertNull(cacheClient.get("company-1"));
        assertNull(cacheClient.get("company-2"));
        assertNull(cacheClient.get("company-3"));
    }

    @Test
    public void clearTestCompaniesList()
    {
        List<TestCompany> companies = testDAO.getCompanies(new int[]{1, 2, 3});
        assertNotNull(cacheClient.get("company-1"));
        assertNotNull(cacheClient.get("company-2"));
        assertNotNull(cacheClient.get("company-3"));
        testDAO.clearTestCompanies(companies);
        assertNull(cacheClient.get("company-1"));
        assertNull(cacheClient.get("company-2"));
        assertNull(cacheClient.get("company-3"));
    }

    @Test
    public void clearTransformation()
    {
        TestObjectBefore testObject = new TestObjectBefore(11);

        TestObjectAfter after = testDAO.transformObjectMatchingId(testObject);
        assertNotNull(cacheClient.get("transform-11"));
        testDAO.clearTransformation(testObject);
        assertNull(cacheClient.get("transform-11"));
    }

    @Test
    public void clearTransformations()
    {
        Collection testObjects = List.of(new TestObjectBefore(11), new TestObjectBefore(12), new TestObjectBefore(13));

        testDAO.transformObjectMatchingIds(testObjects);
        assertNotNull(cacheClient.get("transform-11"));
        assertNotNull(cacheClient.get("transform-12"));
        assertNotNull(cacheClient.get("transform-13"));
        testDAO.clearTransformations(testObjects);
        assertNull(cacheClient.get("transform-11"));
        assertNull(cacheClient.get("transform-12"));
        assertNull(cacheClient.get("transform-13"));
    }

    @Test
    public void handleNull()
    {
        testDAO.handleNull(null);
//        assertNotNull(cacheClient.get("transform-11"));
    }
/*
        void clearTransformation(TestObjectBefore testObject);
        void clearTransformations(Collection<TestObjectBefore> testObjects);
 */
}