package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.GradeScales
import com.example.data.model.SubjectEntity
import com.example.ui.components.GradeBadge
import com.example.ui.theme.CyanAccent
import com.example.ui.viewmodel.GradeTrackViewModel

@Composable
fun SubjectsManagerScreen(
    viewModel: GradeTrackViewModel,
    onAddSubjectRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allSubjects by viewModel.allSubjects.collectAsStateWithLifecycle()
    val profile by viewModel.studentProfile.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedSemesterTab by remember { mutableStateOf<Int?>(profile.currentSemester) }

    var subjectToEdit by remember { mutableStateOf<SubjectEntity?>(null) }
    var subjectToDelete by remember { mutableStateOf<SubjectEntity?>(null) }

    val filteredSubjects = allSubjects.filter { subj ->
        val matchesSearch = subj.name.contains(searchQuery, ignoreCase = true) ||
                subj.code.contains(searchQuery, ignoreCase = true)
        val matchesSemester = selectedSemesterTab == null || subj.semester == selectedSemesterTab
        matchesSearch && matchesSemester
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Screen Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Subject Marks Manager",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Course syllabus, credits, examination marks & grade breakdown",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onAddSubjectRequest,
                    modifier = Modifier.testTag("add_subject_fab"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add")
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_subjects_input"),
                placeholder = { Text("Search by subject name or code (e.g. CS501)...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )
        }

        // Semester Filter Tabs
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    val isAll = selectedSemesterTab == null
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isAll) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                            .clickable { selectedSemesterTab = null }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "All Semesters",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isAll) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                items(8) { idx ->
                    val sem = idx + 1
                    val isSelected = selectedSemesterTab == sem
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                            .clickable { selectedSemesterTab = sem }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Sem $sem",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // List of Subjects
        if (filteredSubjects.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No subjects match your query.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(filteredSubjects) { subject ->
                SubjectMarksDetailCard(
                    subject = subject,
                    onEdit = { subjectToEdit = subject },
                    onDelete = { subjectToDelete = subject }
                )
            }
        }
    }

    // Edit Subject Dialog
    if (subjectToEdit != null) {
        SubjectEditModal(
            initialSubject = subjectToEdit!!,
            gradingSystem = profile.gradingSystem,
            onDismiss = { subjectToEdit = null },
            onSave = { updated ->
                viewModel.updateSubject(updated)
                subjectToEdit = null
            }
        )
    }

    // Delete Subject Confirmation
    if (subjectToDelete != null) {
        val target = subjectToDelete!!
        AlertDialog(
            onDismissRequest = { subjectToDelete = null },
            title = { Text("Delete Subject?") },
            text = { Text("Are you sure you want to delete '${target.name}' (${target.code})?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSubject(target)
                        subjectToDelete = null
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { subjectToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SubjectMarksDetailCard(
    subject: SubjectEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("subject_card_${subject.code}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = subject.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Text(
                        text = "${subject.code} • Sem ${subject.semester} • ${subject.credits} Credits",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                GradeBadge(grade = subject.grade, points = subject.gradePoints)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Marks Breakdown Chips: Internal, Assignment, Practical, Theory
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(vertical = 8.dp, horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Internal", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${subject.internalMarks.toInt()}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Assign", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${subject.assignmentMarks.toInt()}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Practical", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${subject.practicalMarks.toInt()}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Theory", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${subject.theoryMarks.toInt()}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total", style = MaterialTheme.typography.labelSmall, color = CyanAccent)
                    Text("${subject.totalMarks.toInt()}/${subject.maxMarks.toInt()}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = CyanAccent)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer row with actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Weighted Points: ${String.format("%.1f", subject.credits * subject.gradePoints)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SubjectEditModal(
    initialSubject: SubjectEntity,
    gradingSystem: com.example.data.model.GradingSystemType,
    onDismiss: () -> Unit,
    onSave: (SubjectEntity) -> Unit
) {
    var name by remember { mutableStateOf(initialSubject.name) }
    var code by remember { mutableStateOf(initialSubject.code) }
    var semesterStr by remember { mutableStateOf(initialSubject.semester.toString()) }
    var creditsStr by remember { mutableStateOf(initialSubject.credits.toString()) }

    var internalMarksStr by remember { mutableStateOf(initialSubject.internalMarks.toInt().toString()) }
    var assignmentMarksStr by remember { mutableStateOf(initialSubject.assignmentMarks.toInt().toString()) }
    var practicalMarksStr by remember { mutableStateOf(initialSubject.practicalMarks.toInt().toString()) }
    var theoryMarksStr by remember { mutableStateOf(initialSubject.theoryMarks.toInt().toString()) }
    var maxMarksStr by remember { mutableStateOf(initialSubject.maxMarks.toInt().toString()) }

    var selectedGrade by remember { mutableStateOf(initialSubject.grade) }
    var selectedPoints by remember { mutableStateOf(initialSubject.gradePoints) }

    val gradeScale = GradeScales.getScale(gradingSystem)
    var expandedGradeMenu by remember { mutableStateOf(false) }

    // Live total marks computation
    val calculatedTotal = (internalMarksStr.toDoubleOrNull() ?: 0.0) +
            (assignmentMarksStr.toDoubleOrNull() ?: 0.0) +
            (practicalMarksStr.toDoubleOrNull() ?: 0.0) +
            (theoryMarksStr.toDoubleOrNull() ?: 0.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialSubject.id == 0L) "Add New Subject" else "Edit Subject Details") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Subject Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = code,
                            onValueChange = { code = it },
                            label = { Text("Subject Code") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = semesterStr,
                            onValueChange = { semesterStr = it },
                            label = { Text("Semester (1-8)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = creditsStr,
                            onValueChange = { creditsStr = it },
                            label = { Text("Credits (e.g. 3, 4)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(
                                onClick = { expandedGradeMenu = true },
                                modifier = Modifier.fillMaxWidth().height(56.dp).padding(top = 8.dp)
                            ) {
                                Text("Grade: $selectedGrade")
                            }

                            DropdownMenu(
                                expanded = expandedGradeMenu,
                                onDismissRequest = { expandedGradeMenu = false }
                            ) {
                                gradeScale.forEach { item ->
                                    DropdownMenuItem(
                                        text = { Text("${item.grade} (${item.points} pts)") },
                                        onClick = {
                                            selectedGrade = item.grade
                                            selectedPoints = item.points
                                            expandedGradeMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Text("Marks Breakdown", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = internalMarksStr,
                            onValueChange = { internalMarksStr = it },
                            label = { Text("Internal") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = assignmentMarksStr,
                            onValueChange = { assignmentMarksStr = it },
                            label = { Text("Assign") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = practicalMarksStr,
                            onValueChange = { practicalMarksStr = it },
                            label = { Text("Practical") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = theoryMarksStr,
                            onValueChange = { theoryMarksStr = it },
                            label = { Text("Theory") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "Computed Total: ${calculatedTotal.toInt()} / ${(maxMarksStr.toDoubleOrNull() ?: 100.0).toInt()}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = CyanAccent
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = initialSubject.copy(
                        name = name.ifBlank { "Subject" },
                        code = code.ifBlank { "SUB101" },
                        semester = semesterStr.toIntOrNull() ?: 1,
                        credits = creditsStr.toIntOrNull() ?: 3,
                        internalMarks = internalMarksStr.toDoubleOrNull() ?: 0.0,
                        assignmentMarks = assignmentMarksStr.toDoubleOrNull() ?: 0.0,
                        practicalMarks = practicalMarksStr.toDoubleOrNull() ?: 0.0,
                        theoryMarks = theoryMarksStr.toDoubleOrNull() ?: 0.0,
                        totalMarks = calculatedTotal,
                        maxMarks = maxMarksStr.toDoubleOrNull() ?: 100.0,
                        grade = selectedGrade,
                        gradePoints = selectedPoints
                    )
                    onSave(updated)
                }
            ) {
                Text("Save Subject")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
