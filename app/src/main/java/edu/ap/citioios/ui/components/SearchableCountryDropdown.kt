package edu.ap.citioios.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import edu.ap.citioios.utils.Countries

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchableCountryDropdown(
    selectedCountry: String,
    onCountrySelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Country",
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null
) {
    var searchQuery by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var hasFocus by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    
    val filteredCountries = remember(searchQuery) {
        Countries.getFilteredCountries(searchQuery)
    }

    LaunchedEffect(selectedCountry) {
        if (!hasFocus && selectedCountry.isNotBlank()) {
            searchQuery = selectedCountry
        }
    }

    Column(modifier = modifier) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { 
                expanded = it
                if (it) {
                    searchQuery = ""
                }
            }
        ) {
            OutlinedTextField(
                value = if (hasFocus) searchQuery else selectedCountry,
                onValueChange = { 
                    searchQuery = it
                    expanded = true
                },
                label = { Text(label) },
                trailingIcon = {
                    Row {
                        if (selectedCountry.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    onCountrySelected("")
                                    searchQuery = ""
                                    expanded = false
                                    focusManager.clearFocus()
                                }
                            ) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Clear selectie"
                                )
                            }
                        }
                        IconButton(
                            onClick = { expanded = !expanded }
                        ) {
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown"
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
                    .onFocusChanged { focusState ->
                        hasFocus = focusState.isFocused
                        if (focusState.isFocused) {
                            expanded = true
                            searchQuery = ""
                        }
                    },
                singleLine = true,
                isError = isError,
                supportingText = supportingText
            )
            
            if (expanded && filteredCountries.isNotEmpty()) {
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { 
                        expanded = false
                        searchQuery = selectedCountry
                    },
                    modifier = Modifier.heightIn(max = 200.dp)
                ) {
                    LazyColumn {
                        items(filteredCountries.take(50)) { country ->
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        text = country,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    ) 
                                },
                                onClick = {
                                    onCountrySelected(country)
                                    searchQuery = country
                                    expanded = false
                                    focusManager.clearFocus()
                                }
                            )
                        }
                        
                        if (filteredCountries.size > 50) {
                            item {
                                Text(
                                    text = "... en ${filteredCountries.size - 50} meer. blijf typen.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
