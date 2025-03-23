import ErrorPopup from "../ErrorPopup";
import { ReactNode } from "react";

interface ModelProps {
  hidePopUp: boolean;
  popUpMessage: string;
  id?: string;
  dialogRef?: React.RefObject<HTMLDialogElement | null>;
}

export default function FormModel({
  id,
  hidePopUp,
  popUpMessage,
  dialogRef,
  children,
}: ModelProps & { children: ReactNode }) {
  const formModelId = "form-model-id";

  return (
    <dialog
      ref={dialogRef}
      id={id ? id : formModelId}
      className="responsive-model"
    >
      <ErrorPopup
        className="absolute left-3/7 left sm:left-auto top-1/4 sm:top-1/9"
        hidden={hidePopUp}
        message={popUpMessage}
      />
      <div className="modal-box">
        <form method="dialog">
          <button className="model-exit-button">✕</button>
        </form>
        {children}
      </div>
    </dialog>
  );
}
