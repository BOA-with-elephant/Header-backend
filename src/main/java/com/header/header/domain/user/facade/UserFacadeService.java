package com.header.header.domain.user.facade;

import com.header.header.auth.model.dto.LoginUserDTO;
import com.header.header.auth.model.dto.SignupDTO;
import com.header.header.common.exception.NotFoundException;
import com.header.header.domain.menu.dto.MenuCategoryDTO;
import com.header.header.domain.menu.service.MenuCategoryService;
import com.header.header.domain.message.service.MessageSendBatchService;
import com.header.header.domain.message.service.MessageTemplateService;
import com.header.header.domain.message.service.ShopMessageHistoryService;
import com.header.header.domain.reservation.dto.BossResvInputDTO;
import com.header.header.domain.reservation.dto.BossResvProjectionDTO;
import com.header.header.domain.reservation.enums.ReservationState;
import com.header.header.domain.reservation.service.BossReservationService;
import com.header.header.domain.sales.dto.SalesDTO;
import com.header.header.domain.sales.service.SalesService;
import com.header.header.domain.user.dto.UserDTO;
import com.header.header.domain.user.entity.User;
import com.header.header.domain.user.repository.MainUserRepository;
import com.header.header.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.logging.Log;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

import java.sql.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserFacadeService {

    private final UserService userService;
    private final MainUserRepository userRepository;
    private final BossReservationService bossReservationService;
    private final SalesService salesService;
    private final MessageSendBatchService messageSendBatchService;
    private final MessageTemplateService messageTemplateService;
    private final MenuCategoryService menuCategoryService;
    private final ShopMessageHistoryService shopMessageHistoryService;

    /* 1. 더미 유저 등록 (전화번호/이름 기반)
    * 업장에서 전화번호 + 이름만으로 임시 가입한 dummy user 생성 */
    @Transactional
    public User registerDummyUser(String userName, String userPhone) {
        return userService.createUserByNameAndPhone(userName, userPhone);
    }

    /* 2. 이름/전화번호로 유저 코드 조회 */
    public Integer getUserCodeByNameAndPhone(String name, String phone) {
        return userService.findUserByNameAndPhone(name, phone);
    }

    /* 3. 회원가입 처리 (중복검사 포함) */
    @Transactional
    public String registerUser(SignupDTO signupDTO) {
        return userService.registerNewUser(signupDTO);
    }

    /* 4. 로그인 시 유저 정보 조회 */
    public LoginUserDTO login(String userId) {
        return userService.findByUserId(userId);
    }

    /* 5. 회원 정보 수정 */
    @Transactional
    public String updateUser(UserDTO dto) {
        return userService.modifyUser(dto);
    }

    /* 6. 회원 탈퇴 처리 (isLeave = true) */
    @Transactional
    public void withdrawUser(UserDTO dto) {
        userService.deleteUser(dto);
    }

    /* 7. 타 엔티티 서비스의 인가 처리 및 확인을 위한 메서드*/
    private void checkAdminAccess() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("회원가입 및 로그인 후 이용가능합니다.");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails userDetails) {
            User currentUser = userRepository.findByUserId(userDetails.getUsername());

            if (currentUser == null) {
                throw new UsernameNotFoundException("해당 유저를 찾을 수 없습니다.");
            }

            if (currentUser.isAdmin() == false) { // isAdmin이 false(일반 고객)라면
                throw new AccessDeniedException("관리자만 접근가능한 페이지입니다.");
            }
        } else {
            throw new AccessDeniedException("샵 등록 후 관리자 전환이 되어야만 접근가능한 페이지입니다.");
        }
    }

    /* 7. BossReservation Authorization 처리 */
    public List<BossResvProjectionDTO> findReservationListForAdmin(Integer shopCode) {
        checkAdminAccess();
        return bossReservationService.findReservationList(shopCode);
    }

    public List<BossResvProjectionDTO> findReservationListByDateForAdmin(Integer shopCode, Date selectedDate) {
        checkAdminAccess();
        return bossReservationService.findReservationListByDate(shopCode, selectedDate);
    }

    public List<BossResvProjectionDTO> findReservationListByNameForAdmin(Integer shopCode, String userName) {
        checkAdminAccess();
        return bossReservationService.findReservationListByName(shopCode, userName);
    }

    public List<BossResvProjectionDTO> findReservationListByMenuNameForAdmin(Integer shopCode, String menuName) {
        checkAdminAccess();
        return bossReservationService.findReservationListByMenuName(shopCode, menuName);
    }

    public BossResvProjectionDTO findReservationByResvCodeForAdmin(Integer resvCode) {
        checkAdminAccess();
        return bossReservationService.findReservationByResvCode(resvCode);
    }

    public void registNewReservationForAdmin(BossResvInputDTO inputDTO) {
        checkAdminAccess();
        bossReservationService.registNewReservation(inputDTO);
    }

    public void updateReservationForAdmin(BossResvInputDTO inputDTO, Integer resvCode) {
        checkAdminAccess();
        bossReservationService.updateReservation(inputDTO, resvCode);
    }

    public void cancelReservationForAdmin(Integer resvCode) {
        checkAdminAccess();
        bossReservationService.cancelReservation(resvCode);
    }

    public void deleteReservationForAdmin(Integer resvCode) {
        checkAdminAccess();
        bossReservationService.deleteReservation(resvCode);
    }

    public void afterProcedureForAdmin(SalesDTO salesDTO) {
        checkAdminAccess();
        bossReservationService.afterProcedure(salesDTO);
    }

    public List<BossResvProjectionDTO> findNoShowListForAdmin(Date today, ReservationState resvState, Integer shopCode) {
        checkAdminAccess();
        return bossReservationService.findNoShowList(today, resvState, shopCode);
    }

    public void noShowHandlerForAdmin(Integer resvCode) {
        checkAdminAccess();
        bossReservationService.noShowHandler(resvCode);
    }


    /* 8. SalesService Authorization 처리 */
    @Transactional
    public void adminSalesLookup(LoginUserDTO loginUserDTO) throws AccessDeniedException {
        User user = userRepository.findByUserId(loginUserDTO.getUserId());
        salesService.accessSales(user);
    }

    /* 9. Message Authorization 처리 */
    @Transactional
    public void adminMsgAuthorize(LoginUserDTO loginUserDTO) {
        User user = userRepository.findByUserId(loginUserDTO.getUserId());
        messageSendBatchService.accessMSB(user);
    }

    /* 10. MsgTemplate 처리 */
    @Transactional
    public void adminMsgTemplateAuthorize(LoginUserDTO loginUserDTO){
        User user = userRepository.findByUserId(loginUserDTO.getUserId());
        messageTemplateService.accessMsgTemplate(user);
    }

    /* 11. MsgHistory 인가 */
    @Transactional
    public void adminMsgHistoryAuthorize(LoginUserDTO loginUserDTO) {
        User user = userRepository.findByUserId(loginUserDTO.getUserId());
        shopMessageHistoryService.c
    }

    /* 12. MenuCategoryService authorization : createMenuCategory, updateMenuCategory, deleteMenuCategory */
    @Transactional
    public MenuCategoryDTO authCreateMenuCategory(LoginUserDTO loginUserDTO, MenuCategoryDTO dto, Integer shopCode) {
        User user = userRepository.findByUserId(loginUserDTO.getUserId());

        // MenuCategorySercie 내의 createMenuCategory 메소드 수정 없이 이용하기 위해
        // 파사드서비스에서의 exception 사용
        if (!user.isAdmin()) {
            throw new AccessDeniedException("관리자만 접근가능한 페이지입니다.");
        }

        return menuCategoryService.createMenuCategory(dto, shopCode);
    }

    @Transactional
    public MenuCategoryDTO authUpdateMenuCategory(LoginUserDTO loginUserDTO, Integer categoryCode, Integer shopCode, MenuCategoryDTO dto) {
        User user = userRepository.findByUserId(loginUserDTO.getUserId());

        if (!user.isAdmin()) {
            throw new AccessDeniedException("관리자만 접근가능한 페이지입니다.");
        }

        return menuCategoryService.updateMenuCategory(categoryCode, shopCode, dto);
    }

    @Transactional
    public void authDeleteMenuCategory(LoginUserDTO loginUserDTO, Integer categoryCode, Integer shopCode) {
        User user = userRepository.findByUserId(loginUserDTO.getUserId());

        if (!user.isAdmin()) {
            throw new AccessDeniedException("관리자만 접근가능한 페이지입니다.");
        }

        menuCategoryService.deleteMenuCategory(categoryCode, shopCode);
    }
}