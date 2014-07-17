package jhas.client.executors;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

import jhas.common.FileSync;

public class DownloadExecutor implements Runnable{
	private List<File> toDownload;
	private FileSync fileSync;
	
	public DownloadExecutor(List<File> list, FileSync fileSync) {
		toDownload = list;
		this.fileSync = fileSync;
	}
	
	@Override
	public void run() {
		if(!toDownload.isEmpty()){
			for(File file : toDownload){
				File backup = null;
				try {
					byte[] newFileBytes = fileSync.download(file);
					if(!Files.exists(file.toPath().getParent())){
						Files.createDirectories(file.toPath().getParent());
					}
					if(file.exists()){
						backup = new File(file.toPath().toString() + ".bak");
						createBackup(file, backup);
					}
					Files.write(file.toPath(), newFileBytes);
					System.out.printf("%s - %s downloaded\n", Thread.currentThread().getName(), file.getName());
				} catch (IOException e) {
					restoreBackup(file, backup);
					e.printStackTrace();
				}finally{
					deleteBackUpFile(backup);
				}
			}
		}
		System.out.println("Download is done!");
	}

	private void deleteBackUpFile(File backup) {
		if(backup != null){
			try {
				Files.delete(backup.toPath());
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage());
			}
		}
	}

	private void createBackup(File file, File backup)
			throws IOException {
		Files.copy(file.toPath(), backup.toPath(), StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
	}

	private void restoreBackup(File file, File backup) {
		if(backup != null){
			try {
				Files.copy(backup.toPath(), file.toPath());
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage());
			}
		}
	}
}
