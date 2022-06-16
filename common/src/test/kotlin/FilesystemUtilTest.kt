//package common.util
//import junit.framework.TestCase.assertEquals
//import org.junit.Test
//
//internal class FilesystemUtilTest {
//    private val testFilesystemUtil: FilesystemUtil = FilesystemUtil()
//
//    @org.junit.jupiter.api.Test
//    fun testTryExtendPath() {
//        val answer = "/tmp/helloworld"
//        val answer1 = "/Users/yogeshbarve/tmp/helloworld"
//        val answer2 = "/Users/tmp/helloworld"
//        val answer3 = "/Users/tmp/helloworld"
//
//        assertEquals(answer,testFilesystemUtil.tryExtendPath("/tmp/helloworld"))
//        assertEquals(answer1,testFilesystemUtil.tryExtendPath("~/tmp/helloworld"))
//        assertEquals(answer2,testFilesystemUtil.tryExtendPath("~/../tmp/helloworld"))
//        assertEquals(answer3,testFilesystemUtil.tryExtendPath("/Users/yogeshbarve/../tmp/helloworld"))
//
//
//
//    }
//}