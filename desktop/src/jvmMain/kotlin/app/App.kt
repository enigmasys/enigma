package app

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import common.model.process.ProcessOwnedItem
import domain.ProcessView
import org.springframework.context.ConfigurableApplicationContext

@Composable
@Preview
fun App(_context: ConfigurableApplicationContext) {
    var text by remember { mutableStateOf("Hello, World!") }
//    var coroutineScope = CoroutineScope(Dispatchers.Main)

//    val processService = _context.getBean(PremonitionProcessServiceImpl::class.java)
//    var processList = mutableStateListOf<String>()

    val stopwatch = remember { ProcessView(_context) }
    stopwatch.start()

    MaterialTheme {


        Column(
            modifier = Modifier.fillMaxSize().padding(start = 60.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally

        ) {


//            stopwatch.processList

//            Row(Modifier.fillMaxSize()) {
//                Box(modifier = Modifier.fillMaxWidth(0.4f), contentAlignment = Alignment.Center) {
//            LazyFunction(stopwatch, stopwatch.processMap, stopwatch::start)
            LazyFunction(stopwatch)
//                }
//                CurrentProcess(stopwatch.processMap,tindex)
        }

    }

//        Button(onClick = {
//            LazyFunction(stopwatch.processList, stopwatch::start)
//
////            coroutineScope.launch {
////                text = processService.getListofProcesses().toString()
////            }
////            text = "Hello World"
//
//        }) {
//            Text(text)
//        }


//    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LazyFunction(
    stopwatch: ProcessView,
) {

    var sections: SnapshotStateMap<String, List<ProcessOwnedItem>> = stopwatch.processMap
    var processStateInfo = stopwatch.processState
    var onStartClick = stopwatch::start
    var tindex by remember { mutableStateOf(0) }
    var tprocessid by remember { mutableStateOf(stopwatch.getFirstProcessID()) }

//    onStartClick()

    Button(onStartClick) {
        Text("Refresh")
    }

    Text(text = "Index value: $tindex", color = Color.Red)

    Row {
        Box(
            modifier = Modifier.width(500.dp)
                .background(color = Color(180, 180, 180))
                .padding(10.dp)
        ) {
            val state = rememberLazyListState()



            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp), state = state
            )
            {
                sections.forEach {
                    stickyHeader {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.LightGray)
                                .padding(12.dp),
                            text = "ProcessType: ${it.key}"
                        )
                        it.value.forEachIndexed { index, processOwnedItem ->
                            Card(elevation = 6.dp, modifier = Modifier.clickable {
                                tindex = index
                                stopwatch.getProcessState(processOwnedItem.processId)
                                tprocessid = processOwnedItem.processId
                            }) {
                                Column(Modifier.padding(8.dp)) {
                                    SelectionContainer {
                                        Text(
                                            text = processOwnedItem.processId,
                                            style = MaterialTheme.typography.body1,
                                            modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth(),
                                            color = MaterialTheme.colors.onSurface,
                                        )
                                    }
                                    Text(
                                        text = processOwnedItem.description,
                                        style = MaterialTheme.typography.body2,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            VerticalScrollbar(
                modifier = Modifier.fillMaxHeight().align(Alignment.CenterEnd),

                adapter = rememberScrollbarAdapter(
                    scrollState = state
                )
            )

        }

        Box(
            modifier = Modifier.width(500.dp)
                .background(color = Color(180, 180, 180))
                .padding(10.dp)
        ) {
            Card {

            Column(Modifier.padding(8.dp)) {

                Text(
                    "Process ID: ${processStateInfo[tprocessid]?.processId}",
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.padding(8.dp).fillMaxWidth(),
                    color = MaterialTheme.colors.onSurface
                )
                Text(
                    "Process Type: ${processStateInfo[tprocessid]?.processType}",
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.padding(8.dp).fillMaxWidth(),
                    color = MaterialTheme.colors.onSurface
                )
//                Text("${processStateInfo[tprocessid]?.isFunction}")
//                Text("Version Number:${processStateInfo[tprocessid]?.lastVersionIndex}")
                Text(
                    "Number of Observations: ${processStateInfo[tprocessid]?.numObservations}",
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.padding(8.dp).fillMaxWidth(),
                    color = MaterialTheme.colors.onSurface,
                )
            }
        }
        }



    }

}
