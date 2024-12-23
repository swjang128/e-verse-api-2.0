package atemos.everse.api.service;

import atemos.everse.api.config.JwtUtil;
import atemos.everse.api.dto.MenuDto;
import atemos.everse.api.entity.Menu;
import atemos.everse.api.repository.MenuRepository;
import atemos.everse.api.specification.MenuSpecification;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * MenuServiceImpl 클래스는 메뉴 관리 기능을 제공하는 서비스 클래스입니다.
 * 새로운 메뉴를 등록, 수정, 삭제하거나, 조건에 맞는 메뉴를 조회하는 기능을 수행합니다.
 */
@Service
@Slf4j
@AllArgsConstructor
public class MenuServiceImpl implements MenuService {
    private final MenuRepository menuRepository;
    private JwtUtil jwtUtil;

    /**
     * 새로운 메뉴를 등록합니다.
     *
     * @param createMenuDto 메뉴를 생성하기 위한 데이터 전송 객체입니다.
     * @return 등록된 메뉴 정보를 담고 있는 객체입니다.
     */
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public MenuDto.ReadMenuResponse create(MenuDto.CreateMenu createMenuDto) {
        // 현재 인증된 사용자의 정보에서 타임존 가져오기
        var zoneId = jwtUtil.getCurrentMember().getCompany().getCountry().getZoneId();
        // 새로운 메뉴 객체 생성
        var menu = Menu.builder()
                .name(createMenuDto.getName())
                .url(createMenuDto.getUrl())
                .description(createMenuDto.getDescription())
                .available(createMenuDto.getAvailable())
                .accessibleRoles(createMenuDto.getAccessibleRoles())
                .depth(0) // 기본적으로 루트 메뉴로 설정
                .build();
        // 상위 메뉴가 지정된 경우, 상위 메뉴 정보 설정 및 depth 갱신
        if (createMenuDto.getParentId() != null) {
            var parentMenu = menuRepository.findById(createMenuDto.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException("No such parent menu."));
            menu.setParent(parentMenu);
            menu.setDepth(parentMenu.getDepth() + 1);
        }
        // 메뉴 저장 및 저장된 메뉴 정보 반환
        var savedMenu = menuRepository.save(menu);
        return new MenuDto.ReadMenuResponse(savedMenu, zoneId);
    }

    /**
     * 조건에 맞는 메뉴들을 조회합니다.
     * 시스템에 등록된 조건에 맞는 메뉴 정보를 조회합니다.
     *
     * @param readMenuRequest 메뉴 조회 조건을 담고 있는 객체입니다.
     * @return 조건에 맞는 메뉴 목록을 포함하는 응답 객체입니다.
     */
    @Override
    @Transactional(readOnly = true)
    public List<MenuDto.ReadMenuResponse> read(MenuDto.ReadMenuRequest readMenuRequest) {
        // 조건에 맞는 메뉴 목록을 조회하여 ID 오름차순으로 정렬
        var sortByIdAsc = Sort.by(Sort.Order.asc("id"));
        var menus = menuRepository.findAll(MenuSpecification.findWith(readMenuRequest), sortByIdAsc);
        return buildHierarchy(menus);
    }

    /**
     * 기존 메뉴를 수정합니다.
     *
     * @param menuId 수정할 메뉴의 ID입니다.
     * @param updateMenuDto 메뉴 수정을 위한 데이터 전송 객체입니다.
     * @return 수정된 메뉴 정보를 담고 있는 객체입니다.
     */
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public MenuDto.ReadMenuResponse update(Long menuId, MenuDto.UpdateMenu updateMenuDto) {
        // 현재 인증된 사용자의 정보에서 타임존 가져오기
        var zoneId = jwtUtil.getCurrentMember().getCompany().getCountry().getZoneId();
        // 해당 메뉴가 존재하는지 확인
        var menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new EntityNotFoundException("No such menu."));
        // 상위 메뉴가 존재하는지 확인하고 설정
        var parentMenu = Optional.ofNullable(updateMenuDto.getParentId())
                .map(parentId -> menuRepository.findById(parentId)
                        .orElseThrow(() -> new EntityNotFoundException("No such parent menu.")))
                .orElse(menu.getParent());
        // 상위 메뉴와 깊이(Depth) 설정
        menu.setParent(parentMenu);
        menu.setDepth(parentMenu != null ? parentMenu.getDepth() + 1 : 0);
        // 메뉴 정보 업데이트
        Optional.ofNullable(updateMenuDto.getName()).ifPresent(menu::setName);
        Optional.ofNullable(updateMenuDto.getUrl()).ifPresent(menu::setUrl);
        Optional.ofNullable(updateMenuDto.getDescription()).ifPresent(menu::setDescription);
        Optional.ofNullable(updateMenuDto.getAvailable()).ifPresent(menu::setAvailable);
        Optional.ofNullable(updateMenuDto.getAccessibleRoles()).ifPresent(menu::setAccessibleRoles);
        // 엔티티 저장 후 리턴
        var updatedMenu = menuRepository.save(menu);
        return new MenuDto.ReadMenuResponse(updatedMenu, zoneId);
    }

    /**
     * 기존 메뉴를 삭제합니다.
     *
     * @param menuId 삭제할 메뉴의 ID입니다.
     */
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void delete(Long menuId) {
        var menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new EntityNotFoundException("No such menu."));
        menuRepository.delete(menu);
    }

    /**
     * 메뉴 목록을 계층 구조로 변환합니다.
     *
     * @param menus 메뉴 목록
     * @return 계층 구조로 변환된 메뉴 목록
     */
    List<MenuDto.ReadMenuResponse> buildHierarchy(List<Menu> menus) {
        // 현재 인증된 사용자의 정보에서 타임존 가져오기
        var zoneId = jwtUtil.getCurrentMember().getCompany().getCountry().getZoneId();
        // Parent ID를 기준으로 메뉴를 그룹화
        var groupedByParentId = menus.stream()
                .collect(Collectors.groupingBy(menu -> menu.getParent() != null ? menu.getParent().getId() : -1L));
        // 루트 메뉴들 (Parent ID가 null인 메뉴들)
        var rootMenus = groupedByParentId.getOrDefault(-1L, new ArrayList<>()).stream()
                .map(menu -> new MenuDto.ReadMenuResponse(menu, zoneId))
                .collect(Collectors.toList());
        // 각 루트 메뉴에 자식 메뉴들을 재귀적으로 설정
        for (var rootMenu : rootMenus) {
            setChildren(rootMenu, groupedByParentId, zoneId);
        }
        return rootMenus;
    }

    /**
     * 루트 메뉴에 자식 메뉴를 설정하는 재귀 메서드입니다.
     *
     * @param parentMenu 부모 메뉴
     * @param groupedByParentId Parent ID로 그룹화된 메뉴 목록
     * @param zoneId 사용자 타임존
     */
    private void setChildren(MenuDto.ReadMenuResponse parentMenu, Map<Long, List<Menu>> groupedByParentId, ZoneId zoneId) {
        // parentMenu의 ID를 기준으로 자식 메뉴 리스트를 가져와서 MenuDto.ReadMenuResponse로 변환
        var children = groupedByParentId.getOrDefault(parentMenu.getMenuId(), new ArrayList<>()).stream()
                .map(menu -> new MenuDto.ReadMenuResponse(menu, zoneId))
                .collect(Collectors.toSet());
        // 부모 메뉴에 자식 메뉴 리스트를 설정
        parentMenu.setChildren(children);
        // 각 자식 메뉴에 대해 재귀적으로 setChildren을 호출하여 그 자식 메뉴들의 자식들도 설정
        for (var childMenu : children) {
            setChildren(childMenu, groupedByParentId, zoneId);
        }
    }
}