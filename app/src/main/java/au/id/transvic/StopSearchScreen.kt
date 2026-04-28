package au.id.transvic

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun StopSearchScreen(onStopSelected: (StopEntity) -> Unit) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf(listOf<StopEntity>()) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("transvic", style = MaterialTheme.typography.headlineMedium)
        Text("Find a stop", style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { q ->
                query = q
                if (q.length >= 2) {
                    // Search automatically as you type (after 2 characters)
                    loading = true
                    scope.launch {
                        results = withContext(Dispatchers.IO) {
                            TransvicDatabase.getInstance(context)
                                .stopDao().search(q)
                        }
                        loading = false
                    }
                } else {
                    results = emptyList()
                }
            },
            label = { Text("Search stops e.g. Flinders Street") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(8.dp))

        if (loading) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }
        }

        LazyColumn {
            items(results) { stop ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onStopSelected(stop) }
                        .padding(vertical = 12.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stop.stopName,
                            style = MaterialTheme.typography.bodyLarge)
                        Text("Stop ID: ${stop.stopId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                HorizontalDivider()
            }
        }
    }
}