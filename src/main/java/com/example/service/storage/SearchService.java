package com.example.service.storage;

import com.example.dto.ResourceInfoDto;
import com.example.dto.enums.ResourceType;
import com.example.exception.StorageException;
import com.example.mapper.ResourceInfoMapper;
import com.example.models.StorageKey;
import com.example.service.minio.StorageService;
import io.minio.messages.Item;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.StreamSupport;

@Service
@AllArgsConstructor
public class SearchService {

    private final StorageService storageService;
    private final ResourceInfoMapper resourceInfoMapper;

    public List<ResourceInfoDto> search(StorageKey storageKey, String query) {
        String queryForMinio = storageKey.buildKey() + query;

        var a = StreamSupport.stream(storageService.listObjects(storageKey, true).spliterator(), false)
                .map(result -> {
                    try {
                        Item item = result.get();
                        String objectName = item.objectName();
                        StorageKey storageKeyInfo = StorageKey.parsePath(objectName);
                        return storageKeyInfo.getResourceType().equals(ResourceType.FILE)
                                ? resourceInfoMapper.toResourceInfoDto(storageKeyInfo, item.size())
                                : resourceInfoMapper.toResourceInfoDto(storageKeyInfo);
                    } catch (Exception ex) {
                        throw new StorageException("Unexpected issue");
                    }
                })
                .toList();
        var b =1;

       return StreamSupport.stream(storageService.listObjects(storageKey, true).spliterator(), false)
               .map(result -> {
                   try {
                       Item item = result.get();
                       String objectName = item.objectName();
                       StorageKey storageKeyInfo = StorageKey.parsePath(objectName);
                       return storageKeyInfo.getResourceType().equals(ResourceType.FILE)
                               ? resourceInfoMapper.toResourceInfoDto(storageKeyInfo, item.size())
                               : resourceInfoMapper.toResourceInfoDto(storageKeyInfo);
                   } catch (Exception ex) {
                       throw new StorageException("Unexpected issue");
                   }
               })
               .filter(resourceInfoDto -> resourceInfoDto.getName().toLowerCase().contains(query.toLowerCase()))
               .toList();
//               StreamSupport.stream(storageService.listObjects(queryForMinio, false).spliterator(), false)
//                .map(result -> {
//                    try {
//                        Item item = result.get();
//                        String objectName = item.objectName();
//                        StorageKey storageKeyInfo = StorageKey.parsePath(objectName);
//                        return storageKeyInfo.getResourceType().equals(ResourceType.FILE)
//                                ? resourceInfoMapper.toResourceInfoDto(storageKeyInfo, item.size())
//                                : resourceInfoMapper.toResourceInfoDto(storageKeyInfo);
//                    } catch (Exception ex) {
//                        throw new StorageException("Unexpected issue");
//                    }
//                })
//               .filter(resourceInfoDto -> resourceInfoDto.getName().toLowerCase().contains(query.toLowerCase()))
//                .toList();
    }
}
