//package edu.vanderbilt.enigma.controller
//
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.beans.factory.annotation.Value
//import org.springframework.web.bind.annotation.GetMapping
//import org.springframework.web.bind.annotation.RequestMapping
//import org.springframework.web.bind.annotation.RestController
//
//
//
//import edu.vanderbilt.enigma.config.TestClientConfig
//import edu.vanderbilt.enigma.services.TestClientService
//
//
//@RestController
//class SampleController {
//
//
////    @Autowired
////    private lateinit var ProcessServiceObj: PremonitionProcessService
//
//    @Autowired
//    private lateinit var obj: TestClientService
////    @Autowired
////    var premWebClient = WebClientConfig()
//
//
//    @RequestMapping("/")
//    fun index() = "This is home!"
//
//    @GetMapping("/premonition")
//    fun Admin(): String? {
//        return "This is success"
//    }
//
//    @GetMapping("/prem")
//    fun msg(): String? {
//
//    return obj.getTestMessage()
//
//    }
//
//}