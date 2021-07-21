/*
 * Copyright (C) 2021 Matthieu Bouquet <matthieu@forestchat.org>
 *
 * This file is part of ForestChat.
 *
 * ForestChat is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ForestChat is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ForestChat.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.forest.forestchat.ui.conversation

import android.Manifest
import android.app.role.RoleManager
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Telephony
import android.view.View
import android.webkit.MimeTypeMap
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import com.forest.forestchat.R
import com.forest.forestchat.app.TransversalBusEvent
import com.forest.forestchat.extensions.observe
import com.forest.forestchat.ui.base.fragment.NavigationFragment
import com.forest.forestchat.ui.conversation.models.ConversationEvent
import com.zhuinden.liveevent.observe
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ConversationFragment : NavigationFragment() {

    companion object {
        private const val CameraDestinationKey = "camera_destination"
    }

    private val viewModel: ConversationViewModel by viewModels()

    private val navigationView: ConversationNavigationView
        get() = view as ConversationNavigationView

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { }

    private var cameraDestination: Uri? = null

    private val getCameraPicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) {
            cameraDestination?.let { viewModel.addImageAttachment(listOf(it)) }
        }

    private val getGalleryPicture =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            viewModel.addImageAttachment(uris)
        }

    private val getContact =
        registerForActivityResult(ActivityResultContracts.PickContact()) { uri ->
            getVCard(uri)?.let { viewModel.addContactAttachment(it) }
        }

    override fun buildNavigationView(): View = ConversationNavigationView(requireContext())

    override fun getStatusBarBgColor(): Int = R.color.background

    override fun getNavigationBarBgColor(): Int = R.color.background

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(navigationView) {
            onMessageEvent = viewModel::onEvent
            optionSelected = viewModel::onMessageOptionSelected
            onTextToSendChange = viewModel::onTextToSendChange
            sendOrAddAttachment = viewModel::sendOrAddAttachment
            onAttachmentSelected = viewModel::attachmentSelected
            toggleAddAttachment = viewModel::toggleAddAttachment
            onInputContentSelected = viewModel::inputContentSelected
            toggleSimCard = viewModel::toggleSim
            onCallClick = viewModel::makeACall
            onRemoveAttachment = viewModel::removeAttachment
        }

        with(viewModel) {
            observe(isLoading(), navigationView::setLoading)
            observe(title(), navigationView::updateTitle)
            observe(state(), navigationView::updateState)
            observe(messageToSend(), navigationView::updateMessageToSend)
            observe(attachmentVisibility(), navigationView::updateAttachmentVisibility)
            observe(activateSending(), navigationView::activateSending)
            observe(simInfo(), navigationView::updateSimInformation)
            observe(attachments(), navigationView::updateAttachments)
            eventSource().observe(viewLifecycleOwner) { event ->
                when (event) {
                    ConversationEvent.RequestDefaultSms -> requestDefaultSmsPermission()
                    ConversationEvent.RequestSmsPermission -> requestSmsPermission()
                    ConversationEvent.RequestStoragePermission -> requestStoragePermission()
                    ConversationEvent.RequestCamera -> requestCamera()
                    ConversationEvent.RequestGallery -> requestGallery()
                    ConversationEvent.RequestContact -> requestContact()
                    is ConversationEvent.ShowFile -> showFile(event.file)
                    is ConversationEvent.Call -> call(event.address, event.asPermissionToCall)
                    else -> null
                }
                navigationView.event(event)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @Suppress("unused")
    fun onTransversalEvent(event: TransversalBusEvent) {
        viewModel.updateMessages()
    }

    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            0
        )
    }

    private fun showFile(file: File) {
        val data = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            file
        )
        val type = MimeTypeMap
            .getSingleton()
            .getMimeTypeFromExtension(file.name.split(".").last())
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(data, type)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        startActivityExternal(intent)
    }

    private fun requestCamera() {
        cameraDestination = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            .let { timestamp ->
                ContentValues().apply {
                    put(
                        MediaStore.Images.Media.TITLE,
                        timestamp
                    )
                }
            }
            .let { cv ->
                context?.contentResolver?.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    cv
                )
            }
        getCameraPicture.launch(cameraDestination)
    }

    private fun requestGallery() {
        getGalleryPicture.launch("image/*")
    }

    private fun requestContact() {
        getContact.launch(null)
    }

    private fun requestDefaultSmsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager =
                requireActivity().getSystemService(RoleManager::class.java) as RoleManager
            resultLauncher.launch(roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS))
        } else {
            val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, requireActivity().packageName)
            requireActivity().startActivity(intent)
        }
    }

    private fun requestSmsPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(), arrayOf(
                Manifest.permission.READ_SMS,
                Manifest.permission.SEND_SMS
            ), 0
        )
    }

    private fun startActivityExternal(intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            resultLauncher.launch(intent)
        } else {
            requireActivity().startActivity(intent)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(CameraDestinationKey, cameraDestination)
        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        cameraDestination = savedInstanceState?.getParcelable(CameraDestinationKey)
        super.onViewStateRestored(savedInstanceState)
    }

    private fun getVCard(contactData: Uri): String? {
        val lookupKey =
            context?.contentResolver?.query(contactData, null, null, null, null)?.use { cursor ->
                cursor.moveToFirst()
                cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY))
            }

        val vCardUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_VCARD_URI, lookupKey)
        return context?.contentResolver?.openAssetFileDescriptor(vCardUri, "r")
            ?.createInputStream()
            ?.readBytes()
            ?.let { bytes -> String(bytes) }
    }

    private fun call(address: String, permissionToCall: Boolean) {
        val action = when (permissionToCall) {
            true -> Intent.ACTION_CALL
            false -> Intent.ACTION_DIAL
        }
        val intent = Intent(action, Uri.parse("tel:$address"))
        startActivityExternal(intent)
    }

}